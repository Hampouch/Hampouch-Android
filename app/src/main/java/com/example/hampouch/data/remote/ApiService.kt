package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.EmailSendData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.data.remote.dto.EmailVerifyData
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.LoginData
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.NicknameCheckData
import com.example.hampouch.data.remote.dto.PasswordResetRequest
import com.example.hampouch.data.remote.dto.SignUpData
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginData
import com.example.hampouch.data.remote.dto.SocialLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/social")
    suspend fun loginWithSocial(@Body request: SocialLoginRequest): Response<ApiResponse<SocialLoginData>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @POST("api/auth/email/send")
    suspend fun sendEmailVerificationCode(@Body request: EmailSendRequest): Response<ApiResponse<EmailSendData>>

    @POST("api/auth/email/verify")
    suspend fun verifyEmailCode(@Body request: EmailVerifyRequest): Response<ApiResponse<EmailVerifyData>>

    @GET("api/auth/nickname/check")
    suspend fun checkNickname(@Query("nickname") nickname: String): Response<ApiResponse<NicknameCheckData>>

    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<ApiResponse<SignUpData>>

    @PATCH("api/auth/password/reset")
    suspend fun resetPassword(@Body request: PasswordResetRequest): Response<ApiResponse<Unit>>
}
