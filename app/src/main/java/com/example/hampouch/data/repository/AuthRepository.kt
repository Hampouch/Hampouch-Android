package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hampouch.core.config.AuthConfig
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.data.model.AuthProvider
import com.example.hampouch.data.model.AuthSession
import com.example.hampouch.data.model.SocialCredential
import com.example.hampouch.data.model.SocialLoginOutcome
import com.example.hampouch.data.model.User
import com.example.hampouch.data.model.UserRole
import com.example.hampouch.data.remote.ApiException
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.AuthMeData
import com.example.hampouch.data.remote.dto.EmailSendData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.data.remote.dto.EmailVerificationPurpose
import com.example.hampouch.data.remote.dto.EmailVerifyData
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.LogoutRequest
import com.example.hampouch.data.remote.dto.NicknameCheckData
import com.example.hampouch.data.remote.dto.PasswordResetRequest
import com.example.hampouch.data.remote.dto.RefreshTokenRequest
import com.example.hampouch.data.remote.dto.SetNicknameRequest
import com.example.hampouch.data.remote.dto.SignUpData
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginRequest
import com.example.hampouch.ui.login.LoginMockData
import com.example.hampouch.ui.session.UserSession
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "AuthRepository"

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

sealed class SessionStatus {
    data class Valid(val needsNickname: Boolean) : SessionStatus()
    object Invalid : SessionStatus()
    object Unknown : SessionStatus()
}

/**
 * 로그인 세션을 DataStore에 저장/조회하는 앱 전역 저장소.
 * [getInstance]로 어느 파일에서든 같은 인스턴스를 얻어 세션을 읽거나 갱신할 수 있다.
 */
class AuthRepository private constructor(private val context: Context) {

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

    val userSession: Flow<AuthSession?> = context.authDataStore.data.map { preferences ->
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
            val response = NetworkModule.apiService.loginWithSocial(
                SocialLoginRequest(
                    provider = credential.provider.name,
                    providerToken = credential.providerToken
                )
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val me = if (!body.isNewUser) fetchAuthMe(body.tokenType, body.accessToken) else null
                val session = AuthSession(
                    provider = credential.provider,
                    userId = body.user.userId,
                    role = body.user.role,
                    status = body.user.status,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType,
                    nickname = if (body.isNewUser) null else (me?.nickname ?: credential.nickname),
                    email = credential.email,
                    profileImageUrl = credential.profileImageUrl
                )
                if (!body.isNewUser) {
                    saveSession(session)
                }
                Result.success(SocialLoginOutcome(session = session, isNewUser = body.isNewUser))
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "소셜 로그인에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
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
            LoginMockData.register(email = updatedSession.email ?: "", password = "", nickname = nickname)
            updatedSession.email?.let { OnboardingDataStore.reserveForNewAccount(context, it) }
            saveSession(updatedSession)
            return Result.success(updatedSession)
        }
        return try {
            val response = NetworkModule.apiService.setNickname(
                authorization = "${session.tokenType} ${session.accessToken}",
                request = SetNicknameRequest(nickname = nickname)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val updatedSession = session.copy(nickname = body.nickname)
                updatedSession.email?.let { OnboardingDataStore.reserveForNewAccount(context, it) }
                saveSession(updatedSession)
                Result.success(updatedSession)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "닉네임 설정에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
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
            val response = NetworkModule.apiService.login(LoginRequest(email = email, password = password))

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
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "로그인에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
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
    ): Result<EmailSendData> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockSendEmailVerificationCode(email, purpose)
        }
        return try {
            val response = NetworkModule.apiService.sendEmailVerificationCode(
                EmailSendRequest(email = email, purpose = purpose.name)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "인증번호 발송에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
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
    ): Result<EmailVerifyData> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockVerifyEmailCode(email, code, purpose)
        }
        return try {
            val response = NetworkModule.apiService.verifyEmailCode(
                EmailVerifyRequest(email = email, code = code, purpose = purpose.name)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "인증번호를 다시 확인해주세요.",
                        fieldErrors = error?.fieldErrors
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "이메일 인증번호 확인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun checkNicknameAvailability(nickname: String): Result<NicknameCheckData> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockCheckNicknameAvailability(nickname)
        }
        return try {
            val response = NetworkModule.apiService.checkNickname(nickname)

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "닉네임 확인에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "닉네임 중복확인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    suspend fun signUp(email: String, password: String, nickname: String): Result<SignUpData> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockSignUp(email, password, nickname)
        }
        return try {
            val response = NetworkModule.apiService.signUp(
                SignUpRequest(email = email, password = password, nickname = nickname)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                OnboardingDataStore.reserveForNewAccount(context, email)
                Result.success(body)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "회원가입에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
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
            return if (LoginMockData.updatePassword(email, newPassword)) {
                Result.success(Unit)
            } else {
                Result.failure(
                    ApiException(code = "EMAIL_NOT_FOUND", message = "사용자를 찾을 수 없습니다.")
                )
            }
        }
        return try {
            val response = NetworkModule.apiService.resetPassword(
                PasswordResetRequest(email = email, newPassword = newPassword)
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "비밀번호 재설정에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
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
            val response = NetworkModule.apiService.getMe(authorization = "$tokenType $accessToken")
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
            val response = NetworkModule.apiService.getMe(
                authorization = "${session.tokenType} ${session.accessToken}"
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

    /**
     * access token이 만료됐을 때 refresh token으로 새 토큰 쌍을 발급받아 세션에 반영한다.
     * refresh token 자체가 무효/만료/폐기됐거나 탈퇴한 회원이면(401/403) 로컬 세션을 지워
     * 다시 로그인하도록 한다.
     */
    suspend fun refreshAccessToken(): Result<AuthSession> {
        val session = userSession.first()
            ?: return Result.failure(ApiException(code = "NO_SESSION", message = "로그인 정보가 없습니다."))
        if (!AuthConfig.USE_SERVER_AUTH) {
            return Result.success(session)
        }
        return try {
            val response = NetworkModule.apiService.refreshToken(
                RefreshTokenRequest(refreshToken = session.refreshToken)
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                // refresh 요청이 서버를 왕복하는 사이 로그아웃/회원탈퇴로 세션이 지워지거나
                // 다른 요청이 먼저 재발급을 마쳤을 수 있다. 그 사이 바뀌었다면 방금 받은 새
                // 토큰으로 되살리지 않고 실패로 처리한다(로그아웃 상태가 되살아나는 것을 방지).
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
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                if (response.code() == 401 || response.code() == 403) {
                    clearSession()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "토큰 재발급에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "토큰 재발급 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    /**
     * 서버에 refresh token 폐기를 요청한 뒤 로컬 세션을 지운다. 서버 호출이 실패하더라도
     * (access token 만료, 네트워크 오류 등) 사용자 의도대로 기기에서는 로그아웃 상태로 만든다.
     */
    suspend fun logout(): Result<Unit> {
        val session = userSession.first()
        if (!AuthConfig.USE_SERVER_AUTH || session == null) {
            clearSession()
            return Result.success(Unit)
        }
        return try {
            val response = NetworkModule.apiService.logout(
                authorization = "${session.tokenType} ${session.accessToken}",
                request = LogoutRequest(refreshToken = session.refreshToken)
            )
            clearSession()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "로그아웃에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "로그아웃 네트워크 오류", e)
            clearSession()
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    /**
     * 회원 탈퇴. 서버에서 계정 삭제가 성공했을 때만 로컬 세션을 지운다.
     * (탈퇴 실패 시 계정이 남아있으므로 로그인 상태를 유지해야 한다.)
     */
    suspend fun withdraw(): Result<Unit> {
        val session = userSession.first()
            ?: return Result.failure(ApiException(code = "NO_SESSION", message = "로그인 정보가 없습니다."))
        if (!AuthConfig.USE_SERVER_AUTH) {
            clearSession()
            return Result.success(Unit)
        }
        return try {
            val response = NetworkModule.apiService.withdraw(
                authorization = "${session.tokenType} ${session.accessToken}"
            )
            if (response.isSuccessful) {
                clearSession()
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "회원 탈퇴에 실패했습니다.",
                        fieldErrors = error?.fieldErrors
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "회원 탈퇴 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    private suspend fun mockLogin(email: String, password: String): Result<AuthSession> {
        val account = LoginMockData.findAccount(email, password)
            ?: return Result.failure(
                ApiException(code = "INVALID_CREDENTIALS", message = "이메일 및 비밀번호를 다시 확인해주세요.")
            )
        return mockSignIn(AuthProvider.LOCAL, account)
    }

    private suspend fun mockLoginWithSocial(credential: SocialCredential): Result<SocialLoginOutcome> {
        val account = LoginMockData.accounts.find { it.email == credential.email }
        if (account != null) {
            return mockSignIn(credential.provider, account).map { session ->
                SocialLoginOutcome(session = session, isNewUser = false)
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
            profileImageUrl = credential.profileImageUrl
        )
        return Result.success(SocialLoginOutcome(session = session, isNewUser = true))
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
        val isRegistered = LoginMockData.isRegistered(email)
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
    ): Result<EmailSendData> {
        checkEmailEligibility(email, purpose)?.let { return Result.failure(it) }
        return Result.success(EmailSendData(expiresInSeconds = 180))
    }

    private fun mockVerifyEmailCode(
        email: String,
        code: String,
        purpose: EmailVerificationPurpose
    ): Result<EmailVerifyData> {
        checkEmailEligibility(email, purpose)?.let { return Result.failure(it) }
        return if (code == "123456") {
            Result.success(EmailVerifyData(email = email, purpose = purpose.name, verified = true))
        } else {
            Result.failure(ApiException(code = "INVALID_CODE", message = "인증번호를 다시 확인해주세요. (목데이터 모드: 123456)"))
        }
    }

    private fun mockCheckNicknameAvailability(nickname: String): Result<NicknameCheckData> {
        val taken = LoginMockData.accounts.any { it.name == nickname }
        return Result.success(NicknameCheckData(nickname = nickname, available = !taken))
    }

    private fun mockSignUp(email: String, password: String, nickname: String): Result<SignUpData> {
        LoginMockData.register(email = email, password = password, nickname = nickname)
        OnboardingDataStore.reserveForNewAccount(context, email)
        return Result.success(
            SignUpData(
                userId = email.hashCode().toLong(),
                email = email,
                nickname = nickname,
                provider = AuthProvider.LOCAL.name
            )
        )
    }

    suspend fun saveSession(session: AuthSession) {
        context.authDataStore.edit { preferences ->
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
        UserSession.login(context, sessionToUser(session))
        AccountDataCoordinator.syncIfNeeded(context, session.userId.toString(), session.email)
    }

    private fun sessionToUser(session: AuthSession): User = User(
        id = session.userId.toString(),
        name = session.nickname ?: session.email ?: "회원",
        email = session.email ?: "",
        password = "",
        role = runCatching { UserRole.valueOf(session.role) }.getOrDefault(UserRole.NORMAL)
    )

    suspend fun currentAuthHeader(): String? {
        val session = userSession.first() ?: return null
        return "${session.tokenType} ${session.accessToken}"
    }

    suspend fun clearSession() {
        context.authDataStore.edit { it.clear() }
        UserSession.logout(context)
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
    }
}
