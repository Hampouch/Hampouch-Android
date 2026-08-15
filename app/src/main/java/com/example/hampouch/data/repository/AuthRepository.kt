package com.example.hampouch.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.hampouch.core.config.AuthConfig
import com.example.hampouch.core.network.AuthTokenProvider
import com.example.hampouch.domain.model.AuthProvider
import com.example.hampouch.domain.model.AuthSession
import com.example.hampouch.domain.model.SocialCredential
import com.example.hampouch.domain.model.SocialLoginOutcome
import com.example.hampouch.data.remote.AuthApi
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.core.network.PendingAuth
import com.example.hampouch.di.AuthDataStore
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.model.UserRole
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.remote.dto.AuthMeData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.domain.model.EmailVerificationPurpose
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.LogoutRequest
import com.example.hampouch.data.remote.dto.PasswordResetRequest
import com.example.hampouch.data.remote.dto.RefreshTokenRequest
import com.example.hampouch.data.remote.dto.SetNicknameRequest
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginRequest
import com.example.hampouch.data.local.AccountMockDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private const val TAG = "AuthRepository"

sealed class SessionStatus {
    data class Valid(val needsNickname: Boolean) : SessionStatus()
    object Invalid : SessionStatus()
    object Unknown : SessionStatus()
}

@Singleton
class AuthRepository @Inject constructor(
    @AuthDataStore private val authDataStore: DataStore<Preferences>,
    private val apiService: AuthApi,
    /** [AccountDataCoordinator]가 이 클래스에 의존해 순환이 생기므로 지연 조회한다. */
    private val accountDataCoordinator: Provider<AccountDataCoordinator>,
    private val onboardingLocalStore: OnboardingLocalStore
) : AuthTokenProvider {

    private object Keys {
        val PROVIDER = stringPreferencesKey("provider")
        val USER_ID = longPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
        val STATUS = stringPreferencesKey("status")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val TOKEN_TYPE = stringPreferencesKey("token_type")
        val NICKNAME = stringPreferencesKey("nickname")
        val EMAIL = stringPreferencesKey("email")
        val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    }

    private val _currentUser = MutableStateFlow(AccountMockDataSource.normalUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val isEditor: Boolean get() = _currentUser.value.role == UserRole.EDITOR

    val userSession: Flow<AuthSession?> = authDataStore.data.map { preferences ->
        val provider = preferences[Keys.PROVIDER]?.let { AuthProvider.valueOf(it) }
        val userId = preferences[Keys.USER_ID]
        val role = preferences[Keys.ROLE]
        val status = preferences[Keys.STATUS]
        val accessToken = preferences[Keys.ACCESS_TOKEN]
        val refreshToken = preferences[Keys.REFRESH_TOKEN]
        val tokenType = preferences[Keys.TOKEN_TYPE]
        if (provider == null || userId == null || role == null || status == null ||
            accessToken == null || refreshToken == null || tokenType == null
        ) {
            null
        } else {
            AuthSession(
                provider = provider,
                userId = userId,
                role = role,
                status = status,
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = tokenType,
                nickname = preferences[Keys.NICKNAME],
                email = preferences[Keys.EMAIL],
                profileImageUrl = preferences[Keys.PROFILE_IMAGE_URL]
            )
        }
    }

    suspend fun loginWithSocial(credential: SocialCredential): Result<SocialLoginOutcome> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockLoginWithSocial(credential)
        }
        return try {
            val response = apiService.loginWithSocial(
                SocialLoginRequest(
                    provider = credential.provider.name,
                    providerToken = credential.providerToken
                )
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val requiresNickname = body.needsNickname || body.isNewUser
                val me = if (!requiresNickname) fetchAuthMe(body.tokenType, body.accessToken) else null
                val session = AuthSession(
                    provider = credential.provider,
                    userId = body.user.userId,
                    role = body.user.role,
                    status = body.user.status,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType,
                    nickname = if (requiresNickname) null else me?.nickname,
                    email = credential.email,
                    profileImageUrl = null
                )
                if (!requiresNickname) {
                    saveSession(session)
                }
                Result.success(
                    SocialLoginOutcome(
                        session = session,
                        isNewUser = body.isNewUser,
                        requiresNickname = requiresNickname
                    )
                )
            } else {
                Result.failure(response.toApiException("소셜 로그인에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "소셜 로그인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun completeSocialSignUp(session: AuthSession, nickname: String): Result<AuthSession> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            val updatedSession = session.copy(nickname = nickname)
            AccountMockDataSource.register(email = updatedSession.email ?: "", password = "", nickname = nickname)
            updatedSession.email?.let { onboardingLocalStore.reserveForNewAccount(it) }
            saveSession(updatedSession)
            return Result.success(updatedSession)
        }
        return try {
            val response = apiService.setNickname(
                pendingAuth = PendingAuth("${session.tokenType} ${session.accessToken}"),
                request = SetNicknameRequest(nickname = nickname)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val updatedSession = session.copy(nickname = body.nickname)
                updatedSession.email?.let { onboardingLocalStore.reserveForNewAccount(it) }
                saveSession(updatedSession)
                Result.success(updatedSession)
            } else {
                Result.failure(response.toApiException("닉네임 설정에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "닉네임 설정 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun login(email: String, password: String): Result<AuthSession> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockLogin(email, password)
        }
        return try {
            val response = apiService.login(LoginRequest(email = email, password = password))

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val me = fetchAuthMe(body.tokenType, body.accessToken)
                val session = AuthSession(
                    provider = AuthProvider.LOCAL,
                    userId = body.user.userId,
                    role = body.user.role,
                    status = body.user.status,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType,
                    nickname = me?.nickname,
                    email = email,
                    profileImageUrl = null
                )
                saveSession(session)
                Result.success(session)
            } else {
                Result.failure(response.toApiException("로그인에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "일반 로그인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun sendEmailVerificationCode(
        email: String,
        purpose: EmailVerificationPurpose
    ): Result<Int> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockSendEmailVerificationCode(email, purpose)
        }
        return try {
            val response = apiService.sendEmailVerificationCode(
                EmailSendRequest(email = email, purpose = purpose.name)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                Result.success(body.expiresInSeconds)
            } else {
                Result.failure(response.toApiException("인증번호 발송에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "이메일 인증번호 발송 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun verifyEmailCode(
        email: String,
        code: String,
        purpose: EmailVerificationPurpose
    ): Result<Unit> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockVerifyEmailCode(email, code, purpose)
        }
        return try {
            val response = apiService.verifyEmailCode(
                EmailVerifyRequest(email = email, code = code, purpose = purpose.name)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                if (body.verified) Result.success(Unit) else {
                    Result.failure(ApiException("INVALID_CODE", "인증번호를 다시 확인해주세요."))
                }
            } else {
                Result.failure(response.toApiException("인증번호를 다시 확인해주세요."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "이메일 인증번호 확인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun checkNicknameAvailability(nickname: String): Result<Boolean> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockCheckNicknameAvailability(nickname)
        }
        return try {
            val response = apiService.checkNickname(nickname)

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                Result.success(body.available)
            } else {
                Result.failure(response.toApiException("닉네임 확인에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "닉네임 중복확인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun signUp(email: String, password: String, nickname: String): Result<Unit> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockSignUp(email, password, nickname)
        }
        return try {
            val response = apiService.signUp(
                SignUpRequest(email = email, password = password, nickname = nickname)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                onboardingLocalStore.reserveForNewAccount(email)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("회원가입에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "회원가입 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return if (AccountMockDataSource.updatePassword(email, newPassword)) {
                Result.success(Unit)
            } else {
                Result.failure(
                    ApiException(code = "EMAIL_NOT_FOUND", message = "사용자를 찾을 수 없습니다.")
                )
            }
        }
        return try {
            val response = apiService.resetPassword(
                PasswordResetRequest(email = email, newPassword = newPassword)
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("비밀번호 재설정에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "비밀번호 재설정 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    private suspend fun fetchAuthMe(tokenType: String, accessToken: String): AuthMeData? {
        return try {
            val response = apiService.getMe(PendingAuth("$tokenType $accessToken"))
            if (response.isSuccessful) response.body()?.data else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "인증/계정 상태 조회 네트워크 오류", e)
            null
        }
    }

    suspend fun checkSessionStatus(session: AuthSession): SessionStatus {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return SessionStatus.Valid(needsNickname = false)
        }
        return try {
            val response = apiService.getMe(
                pendingAuth = PendingAuth("${session.tokenType} ${session.accessToken}")
            )
            val data = response.body()?.data
            when {
                response.isSuccessful && data != null -> {
                    if (data.nickname != session.nickname) {
                        saveSession(session.copy(nickname = data.nickname))
                    }
                    SessionStatus.Valid(needsNickname = data.needsNickname)
                }
                response.code() == 401 || response.code() == 403 -> SessionStatus.Invalid
                else -> SessionStatus.Unknown
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "세션 상태 확인 네트워크 오류", e)
            SessionStatus.Unknown
        }
    }

    suspend fun refreshAccessToken(): Result<AuthSession> {
        val session = userSession.first()
            ?: return Result.failure(ApiException(code = "NO_SESSION", message = "로그인 정보가 없습니다."))
        if (!AuthConfig.USE_SERVER_AUTH) {
            return Result.success(session)
        }
        return try {
            val response = apiService.refreshToken(
                RefreshTokenRequest(refreshToken = session.refreshToken)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val latestSession = userSession.first()
                if (latestSession == null || latestSession.refreshToken != session.refreshToken) {
                    return Result.failure(
                        ApiException(code = "SESSION_CHANGED", message = "세션이 이미 변경되었습니다.")
                    )
                }
                val updatedSession = latestSession.copy(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType
                )
                saveSession(updatedSession)
                Result.success(updatedSession)
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    clearSession()
                }
                Result.failure(response.toApiException("토큰 재발급에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "토큰 재발급 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun logout(): Result<Unit> {
        val session = userSession.first()
        if (!AuthConfig.USE_SERVER_AUTH || session == null) {
            clearSession()
            return Result.success(Unit)
        }
        return try {
            val response = apiService.logout(
                request = LogoutRequest(refreshToken = session.refreshToken)
            )
            clearSession()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("로그아웃에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "로그아웃 네트워크 오류", e)
            clearSession()
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun withdraw(): Result<Unit> {
        val session = userSession.first()
            ?: return Result.failure(ApiException(code = "NO_SESSION", message = "로그인 정보가 없습니다."))
        if (!AuthConfig.USE_SERVER_AUTH) {
            clearSession()
            return Result.success(Unit)
        }
        return try {
            val response = apiService.withdraw()
            if (response.isSuccessful) {
                clearSession()
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("회원 탈퇴에 실패했습니다."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "회원 탈퇴 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    private suspend fun mockLogin(email: String, password: String): Result<AuthSession> {
        val account = AccountMockDataSource.findAccount(email, password)
            ?: return Result.failure(
                ApiException(code = "INVALID_CREDENTIALS", message = "이메일 및 비밀번호를 다시 확인해주세요.")
            )
        return mockSignIn(AuthProvider.LOCAL, account)
    }

    private suspend fun mockLoginWithSocial(credential: SocialCredential): Result<SocialLoginOutcome> {
        val account = AccountMockDataSource.accounts.find { it.email == credential.email }
        if (account != null) {
            return mockSignIn(credential.provider, account).map { session ->
                SocialLoginOutcome(
                    session = session,
                    isNewUser = false,
                    requiresNickname = session.nickname.isNullOrBlank()
                )
            }
        }
        val session = AuthSession(
            provider = credential.provider,
            userId = (credential.email ?: credential.providerToken).hashCode().toLong(),
            role = UserRole.NORMAL.name,
            status = "ACTIVE",
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token",
            tokenType = "Bearer",
            nickname = null,
            email = credential.email,
            profileImageUrl = null
        )
        return Result.success(
            SocialLoginOutcome(session = session, isNewUser = true, requiresNickname = true)
        )
    }

    private suspend fun mockSignIn(provider: AuthProvider, account: User): Result<AuthSession> {
        val session = AuthSession(
            provider = provider,
            userId = account.id.hashCode().toLong(),
            role = account.role.name,
            status = "ACTIVE",
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token",
            tokenType = "Bearer",
            nickname = account.name,
            email = account.email,
            profileImageUrl = null
        )
        saveSession(session)
        return Result.success(session)
    }

    private fun checkEmailEligibility(email: String, purpose: EmailVerificationPurpose): ApiException? {
        val isRegistered = AccountMockDataSource.isRegistered(email)
        return when (purpose) {
            EmailVerificationPurpose.SIGNUP -> if (isRegistered) {
                ApiException(code = "EMAIL_ALREADY_EXISTS", message = "이미 가입된 이메일입니다.")
            } else {
                null
            }
            EmailVerificationPurpose.PASSWORD_RESET -> if (!isRegistered) {
                ApiException(code = "EMAIL_NOT_FOUND", message = "사용자를 찾을 수 없습니다.")
            } else {
                null
            }
        }
    }

    private fun mockSendEmailVerificationCode(
        email: String,
        purpose: EmailVerificationPurpose
    ): Result<Int> {
        checkEmailEligibility(email, purpose)?.let { return Result.failure(it) }
        return Result.success(180)
    }

    private fun mockVerifyEmailCode(
        email: String,
        code: String,
        purpose: EmailVerificationPurpose
    ): Result<Unit> {
        checkEmailEligibility(email, purpose)?.let { return Result.failure(it) }
        return if (code == "123456") {
            Result.success(Unit)
        } else {
            Result.failure(ApiException(code = "INVALID_CODE", message = "인증번호를 다시 확인해주세요. (목데이터 모드: 123456)"))
        }
    }

    private fun mockCheckNicknameAvailability(nickname: String): Result<Boolean> {
        val taken = AccountMockDataSource.accounts.any { it.name == nickname }
        return Result.success(!taken)
    }

    private fun mockSignUp(email: String, password: String, nickname: String): Result<Unit> {
        AccountMockDataSource.register(email = email, password = password, nickname = nickname)
        onboardingLocalStore.reserveForNewAccount(email)
        return Result.success(Unit)
    }

    suspend fun saveSession(session: AuthSession) {
        authDataStore.edit { preferences ->
            preferences[Keys.PROVIDER] = session.provider.name
            preferences[Keys.USER_ID] = session.userId
            preferences[Keys.ROLE] = session.role
            preferences[Keys.STATUS] = session.status
            preferences[Keys.ACCESS_TOKEN] = session.accessToken
            preferences[Keys.REFRESH_TOKEN] = session.refreshToken
            preferences[Keys.TOKEN_TYPE] = session.tokenType
            session.nickname?.let { preferences[Keys.NICKNAME] = it } ?: preferences.remove(Keys.NICKNAME)
            session.email?.let { preferences[Keys.EMAIL] = it } ?: preferences.remove(Keys.EMAIL)
            session.profileImageUrl?.let {
                preferences[Keys.PROFILE_IMAGE_URL] = it
            } ?: preferences.remove(Keys.PROFILE_IMAGE_URL)
        }
        _currentUser.value = sessionToUser(session)
        _isLoggedIn.value = true
        accountDataCoordinator.get().syncIfNeeded(session.userId.toString(), session.email)
    }

    private fun sessionToUser(session: AuthSession): User = User(
        id = session.userId.toString(),
        name = session.nickname ?: session.email ?: "회원",
        email = session.email ?: "",
        password = "",
        role = runCatching { UserRole.valueOf(session.role) }.getOrDefault(UserRole.NORMAL)
    )

    override suspend fun currentAuthHeader(): String? {
        val session = userSession.first() ?: return null
        return "${session.tokenType} ${session.accessToken}"
    }

    suspend fun clearSession() {
        authDataStore.edit { it.clear() }
        _currentUser.value = AccountMockDataSource.normalUser
        _isLoggedIn.value = false
    }

}
