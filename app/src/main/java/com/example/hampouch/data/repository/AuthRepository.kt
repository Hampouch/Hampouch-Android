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
import com.example.hampouch.data.remote.dto.EmailSendData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.data.remote.dto.EmailVerificationPurpose
import com.example.hampouch.data.remote.dto.EmailVerifyData
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.NicknameCheckData
import com.example.hampouch.data.remote.dto.PasswordResetRequest
import com.example.hampouch.data.remote.dto.SetNicknameRequest
import com.example.hampouch.data.remote.dto.SignUpData
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginRequest
import com.example.hampouch.ui.login.LoginMockData
import com.example.hampouch.ui.session.UserSession
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "AuthRepository"

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

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

    /**
     * 소셜 SDK에서 받은 [credential]을 서버(/api/auth/social)로 전달해 로그인 검증을 수행한다.
     *
     * 신규 유저([SocialLoginOutcome.isNewUser] == true)는 서버에 닉네임이 아직 없으므로 SDK가 돌려준
     * 프로필 닉네임은 쓰지 않고 세션도 저장하지 않는다 — 화면단에서 닉네임 입력 다이얼로그를 띄운 뒤
     * [completeSocialSignUp]까지 마쳐야 로그인이 완료된다. 기존 유저는 곧바로 세션을 저장하고 로그인 처리한다.
     */
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
                val session = AuthSession(
                    provider = credential.provider,
                    userId = body.user.userId,
                    role = body.user.role,
                    status = body.user.status,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType,
                    nickname = if (body.isNewUser) null else credential.nickname,
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
                        message = error?.message ?: "소셜 로그인에 실패했습니다."
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 네트워크 오류, BASE_URL 미설정 등은 사용자에게 원문 메시지 대신 안내 문구로 통일해 보여준다.
            Log.e(TAG, "소셜 로그인 네트워크 오류", e)
            Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
        }
    }

    /**
     * 소셜 회원가입 온보딩 마지막 단계: [loginWithSocial]에서 받은 (아직 저장되지 않은) [session]의
     * accessToken으로 서버(/api/auth/nickname)에 최초 닉네임을 등록하고, 성공하면 세션을 저장해
     * 로그인을 완료한다.
     */
    suspend fun completeSocialSignUp(session: AuthSession, nickname: String): Result<AuthSession> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            val updatedSession = session.copy(nickname = nickname)
            // 이메일+비밀번호 회원가입과 마찬가지로 목데이터 계정 목록에 등록해서, 같은 테스트 이메일로
            // 다시 소셜 로그인하면 신규 유저가 아닌 기존 유저로 곧바로 로그인되도록 한다.
            LoginMockData.register(email = updatedSession.email ?: "", password = "", nickname = nickname)
            // 방금 완료한 회원가입이므로, 이 이메일에 예약된 온보딩 값이 있으면 이 계정 전용으로 확정해둔다.
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
                        message = error?.message ?: "닉네임 설정에 실패했습니다."
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

    /**
     * 이메일+비밀번호로 일반 로그인을 서버(/api/auth/login)에 요청하고, 성공 시 세션을 저장한다.
     */
    suspend fun login(email: String, password: String): Result<AuthSession> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockLogin(email, password)
        }
        return try {
            val response = NetworkModule.apiService.login(LoginRequest(email = email, password = password))

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val session = AuthSession(
                    provider = AuthProvider.LOCAL,
                    userId = body.user.userId,
                    role = body.user.role,
                    status = body.user.status,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType,
                    nickname = null,
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
                        message = error?.message ?: "로그인에 실패했습니다."
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

    /**
     * 회원가입/비밀번호 재설정용 이메일 인증번호 발송을 서버(/api/auth/email/send)에 요청한다.
     */
    suspend fun sendEmailVerificationCode(
        email: String,
        purpose: EmailVerificationPurpose
    ): Result<EmailSendData> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return mockSendEmailVerificationCode()
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
                        message = error?.message ?: "인증번호 발송에 실패했습니다."
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

    /**
     * 발송된 이메일 인증번호를 서버(/api/auth/email/verify)로 검증한다.
     */
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
                        message = error?.message ?: "인증번호를 다시 확인해주세요."
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

    /**
     * 닉네임 중복 여부를 서버(/api/auth/nickname/check)에 확인한다.
     */
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
                        message = error?.message ?: "닉네임 확인에 실패했습니다."
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

    /**
     * 이메일+비밀번호로 일반 회원가입을 서버(/api/auth/signup)에 요청한다.
     * 성공해도 로그인 토큰은 내려오지 않으므로, 이어서 로그인 화면에서 별도로 로그인해야 한다.
     */
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
                // 방금 완료한 회원가입이므로, 이 이메일에 예약된 온보딩 값이 있으면 이 계정 전용으로 확정해둔다.
                // (실제 로그인은 이 화면이 아니라 로그인 화면에서 별도로 하므로, 그때 소비된다.)
                OnboardingDataStore.reserveForNewAccount(context, email)
                Result.success(body)
            } else {
                val error = response.errorBody()?.string()?.let {
                    runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
                }
                Result.failure(
                    ApiException(
                        code = error?.code ?: "UNKNOWN",
                        message = error?.message ?: "회원가입에 실패했습니다."
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

    /**
     * 이메일 인증을 마친 뒤 새 비밀번호로 재설정한다(/api/auth/password/reset).
     * 응답에 data가 없으므로 HTTP 성공 여부만으로 판단한다.
     */
    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return Result.success(Unit)
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
                        message = error?.message ?: "비밀번호 재설정에 실패했습니다."
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

    // region 목데이터 모드 (AuthConfig.USE_SERVER_AUTH == false)
    // 서버 없이 화면/네비게이션을 확인할 때 쓰는 더미 구현. LoginMockData의 고정 계정으로만 동작하며,
    // signUp/checkNicknameAvailability 등은 실제로 계정을 만들거나 저장하지 않는다(단순 성공 흉내).

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
            // 이미 목데이터 모드에서 가입된(닉네임 등록까지 마친) 이메일이면 곧바로 로그인 처리한다.
            return mockSignIn(credential.provider, account).map { session ->
                SocialLoginOutcome(session = session, isNewUser = false)
            }
        }
        // 처음 보는 이메일(예: SocialAuthManager의 목데이터 테스트 이메일)이면 실제 서버처럼 신규 유저로 취급해
        // 닉네임 입력 다이얼로그부터 띄운다. 세션은 아직 저장하지 않고 [completeSocialSignUp]에서 마무리한다.
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

    /**
     * 목데이터 세션을 만들고 [saveSession]으로 DataStore에 저장한다(서버 로그인과 동일하게 영속화해야
     * 다음 앱 실행 때도 로그인 상태로 시작 화면이 Home으로 잡힌다).
     */
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

    private fun mockSendEmailVerificationCode(): Result<EmailSendData> =
        Result.success(EmailSendData(expiresInSeconds = 180))

    private fun mockVerifyEmailCode(
        email: String,
        code: String,
        purpose: EmailVerificationPurpose
    ): Result<EmailVerifyData> {
        // 목데이터 모드에서는 인증번호를 실제로 발송하지 않으므로 고정 코드(123456)만 통과시킨다.
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
        // 실제 회원가입처럼 로그인 화면에서 곧바로 로그인할 수 있도록 목데이터 계정 목록에 등록한다.
        LoginMockData.register(email = email, password = password, nickname = nickname)
        // 방금 완료한 회원가입이므로, 이 이메일에 예약된 온보딩 값이 있으면 이 계정 전용으로 확정해둔다.
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
    // endregion

    /**
     * 세션을 DataStore에 저장하고, 역할 기반 화면들이 참조하는 [UserSession]도 함께 갱신한다.
     */
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
        // 로그인/회원가입이 실제로 일어나는 이 시점에 계정별 목데이터 스토어(마이페이지 프로필 등)를 동기화한다.
        // AppNavHost의 콜드 스타트 동기화는 "이미 로그인된 채로 앱을 재시작한 경우"만 커버하고,
        // 앱을 껐다 켜지 않고 로그인/로그아웃만 반복하는 경우는 여기서 처리해야 한다.
        AccountDataCoordinator.syncIfNeeded(context, session.userId.toString(), session.email)
    }

    private fun sessionToUser(session: AuthSession): User = User(
        id = session.userId.toString(),
        name = session.nickname ?: session.email ?: "회원",
        email = session.email ?: "",
        password = "",
        role = runCatching { UserRole.valueOf(session.role) }.getOrDefault(UserRole.NORMAL)
    )

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
