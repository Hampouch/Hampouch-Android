package com.example.hampouch.core.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.hampouch.BuildConfig
import kotlinx.coroutines.CancellationException
import com.example.hampouch.core.config.AuthConfig
import com.example.hampouch.domain.model.AuthProvider
import com.example.hampouch.domain.model.SocialCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

private const val TAG = "SocialAuthManager"

class SocialSignInCancelledException : Exception("사용자가 로그인을 취소했습니다.")

object SocialAuthManager {
    suspend fun signInWithGoogle(context: Context): Result<SocialCredential> {
        if (!AuthConfig.USE_SERVER_AUTH) {
            return Result.success(mockCredential(AuthProvider.GOOGLE))
        }
        val activity = context.findActivity()
            ?: return Result.failure(IllegalStateException("액티비티 컨텍스트를 찾을 수 없습니다."))

        return try {
            val signInWithGoogleOption = GetSignInWithGoogleOption
                .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val response = CredentialManager.create(activity).getCredential(activity, request)
            handleGoogleSignIn(response)
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "구글 로그인 취소", e)
            Result.failure(SocialSignInCancelledException())
        } catch (e: GetCredentialException) {
            Log.e(TAG, "구글 로그인 실패", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "구글 로그인 실패", e)
            Result.failure(e)
        }
    }

    private fun handleGoogleSignIn(response: GetCredentialResponse): Result<SocialCredential> {
        val credential = response.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Log.e(TAG, "예상하지 못한 인증 정보 타입: ${credential.type}")
            return Result.failure(IllegalStateException("지원하지 않는 인증 정보입니다."))
        }
        return try {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Result.success(
                SocialCredential(
                    provider = AuthProvider.GOOGLE,
                    providerToken = googleCredential.idToken,
                    email = googleCredential.email
                )
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "구글 ID 토큰 파싱 실패", e)
            Result.failure(e)
        }
    }

    fun signInWithKakao(context: Context, onResult: (Result<SocialCredential>) -> Unit) {
        if (!AuthConfig.USE_SERVER_AUTH) {
            onResult(Result.success(mockCredential(AuthProvider.KAKAO)))
            return
        }
        val loginContext = context.findActivity() ?: context

        val accountCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                    onResult(Result.failure(error.toSocialSignInError()))
                }

                token != null -> fetchKakaoUserSession(token.accessToken, onResult)

                else -> onResult(Result.failure(IllegalStateException("카카오 토큰을 받지 못했습니다.")))
            }
        }

        try {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(loginContext)) {
                UserApiClient.instance.loginWithKakaoTalk(loginContext) { token, error ->
                    when {
                        error != null -> {
                            Log.w(TAG, "카카오톡으로 로그인 실패", error)
                            if (error.isUserCancelled()) {
                                onResult(Result.failure(SocialSignInCancelledException()))
                            } else {
                                UserApiClient.instance.loginWithKakaoAccount(
                                    loginContext,
                                    callback = accountCallback
                                )
                            }
                        }

                        token != null -> fetchKakaoUserSession(token.accessToken, onResult)

                        else -> UserApiClient.instance.loginWithKakaoAccount(
                            loginContext,
                            callback = accountCallback
                        )
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(loginContext, callback = accountCallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "카카오 로그인 실패", e)
            onResult(Result.failure(e))
        }
    }

    private fun fetchKakaoUserSession(accessToken: String, onResult: (Result<SocialCredential>) -> Unit) {
        UserApiClient.instance.me { user, error ->
            if (error != null || user == null) {
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error)
                onResult(Result.failure(error ?: IllegalStateException("사용자 정보를 가져오지 못했습니다.")))
                return@me
            }
            onResult(
                Result.success(
                    SocialCredential(
                        provider = AuthProvider.KAKAO,
                        providerToken = accessToken,
                        email = user.kakaoAccount?.email
                    )
                )
            )
        }
    }

    private fun mockCredential(provider: AuthProvider): SocialCredential {
        val testEmail = when (provider) {
            AuthProvider.KAKAO -> "kakao.test@test.com"
            AuthProvider.GOOGLE -> "google.test@test.com"
            AuthProvider.LOCAL -> "test@test.com"
        }
        return SocialCredential(
            provider = provider,
            providerToken = "mock-provider-token",
            email = testEmail
        )
    }
}

private fun Throwable.isUserCancelled(): Boolean =
    this is ClientError && reason == ClientErrorCause.Cancelled

private fun Throwable.toSocialSignInError(): Throwable =
    if (isUserCancelled()) SocialSignInCancelledException() else this

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
