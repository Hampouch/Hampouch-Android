package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.data.model.AuthProvider
import com.example.hampouch.data.model.SocialCredential
import com.example.hampouch.data.model.UserSession
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
import com.example.hampouch.data.remote.dto.SignUpData
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginRequest
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

    val userSession: Flow<UserSession?> = context.authDataStore.data.map { preferences ->
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
            UserSession(
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
     * 소셜 SDK에서 받은 [credential]을 서버(/api/auth/social)로 전달해 로그인 검증을 수행하고,
     * 성공 시 발급된 토큰과 사용자 정보를 세션으로 저장한다.
     */
    suspend fun loginWithSocial(credential: SocialCredential): Result<UserSession> {
        return try {
            val response = NetworkModule.apiService.loginWithSocial(
                SocialLoginRequest(
                    provider = credential.provider.name,
                    providerToken = credential.providerToken
                )
            )

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val session = UserSession(
                    provider = credential.provider,
                    userId = body.user.userId,
                    role = body.user.role,
                    status = body.user.status,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenType = body.tokenType,
                    nickname = credential.nickname,
                    email = credential.email,
                    profileImageUrl = credential.profileImageUrl
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
     * 이메일+비밀번호로 일반 로그인을 서버(/api/auth/login)에 요청하고, 성공 시 세션을 저장한다.
     */
    suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val response = NetworkModule.apiService.login(LoginRequest(email = email, password = password))

            val body = response.body()?.data
            if (response.isSuccessful && body != null) {
                val session = UserSession(
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
        return try {
            val response = NetworkModule.apiService.signUp(
                SignUpRequest(email = email, password = password, nickname = nickname)
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

    suspend fun saveSession(session: UserSession) {
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
    }

    suspend fun clearSession() {
        context.authDataStore.edit { it.clear() }
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
