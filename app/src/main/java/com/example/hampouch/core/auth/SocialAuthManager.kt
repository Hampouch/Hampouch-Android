package com.example.hampouch.core.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.hampouch.BuildConfig
import com.example.hampouch.data.model.AuthProvider
import com.example.hampouch.data.model.UserSession
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

private const val TAG = "SocialAuthManager"

object SocialAuthManager {

    suspend fun signInWithGoogle(context: Context): Result<UserSession> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = CredentialManager.create(context)
                .getCredential(context, request)
                .credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                // TODO: 서버 연결 후 idToken을 백엔드로 전달해 로그인 검증으로 교체
                Result.success(
                    UserSession(
                        provider = AuthProvider.GOOGLE,
                        token = googleCredential.idToken,
                        nickname = googleCredential.displayName,
                        email = googleCredential.email,
                        profileImageUrl = googleCredential.profilePictureUri?.toString()
                    )
                )
            } else {
                Result.failure(IllegalStateException("지원하지 않는 인증 정보입니다."))
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "구글 로그인 실패", e)
            Result.failure(e)
        }
    }

    fun signInWithKakao(context: Context, onResult: (Result<UserSession>) -> Unit) {
        val onKakaoAccountLogin: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> fetchKakaoUserSession(token.accessToken, onResult)
                error != null -> {
                    Log.e(TAG, "카카오 로그인 실패", error)
                    onResult(Result.failure(error))
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                val userCancelled = error is ClientError && error.reason == ClientErrorCause.Cancelled
                when {
                    token != null -> onKakaoAccountLogin(token, null)
                    userCancelled -> onResult(Result.failure(error!!))
                    else -> UserApiClient.instance.loginWithKakaoAccount(context, callback = onKakaoAccountLogin)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = onKakaoAccountLogin)
        }
    }

    private fun fetchKakaoUserSession(accessToken: String, onResult: (Result<UserSession>) -> Unit) {
        UserApiClient.instance.me { user, error ->
            if (error != null || user == null) {
                Log.e(TAG, "카카오 사용자 정보 조회 실패", error)
                onResult(Result.failure(error ?: IllegalStateException("사용자 정보를 가져오지 못했습니다.")))
                return@me
            }
            val account = user.kakaoAccount
            // TODO: 서버 연결 후 accessToken을 백엔드로 전달해 로그인 검증으로 교체
            onResult(
                Result.success(
                    UserSession(
                        provider = AuthProvider.KAKAO,
                        token = accessToken,
                        nickname = account?.profile?.nickname,
                        email = account?.email,
                        profileImageUrl = account?.profile?.profileImageUrl
                    )
                )
            )
        }
    }
}
