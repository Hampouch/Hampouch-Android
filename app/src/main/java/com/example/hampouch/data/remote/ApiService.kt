package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.AddCustomMiniChallengeRequest
import com.example.hampouch.data.remote.dto.AddRecommendedMiniChallengeRequest
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.AuthMeData
import com.example.hampouch.data.remote.dto.EmailSendData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.data.remote.dto.EmailVerifyData
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.LoginData
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.MiniChallengeCheckData
import com.example.hampouch.data.remote.dto.MiniChallengeCheckRequest
import com.example.hampouch.data.remote.dto.MiniChallengeCreatedData
import com.example.hampouch.data.remote.dto.MiniChallengeDayData
import com.example.hampouch.data.remote.dto.NicknameCheckData
import com.example.hampouch.data.remote.dto.PasswordResetRequest
import com.example.hampouch.data.remote.dto.RecommendedMiniChallengeListData
import com.example.hampouch.data.remote.dto.SetNicknameData
import com.example.hampouch.data.remote.dto.SetNicknameRequest
import com.example.hampouch.data.remote.dto.SignUpData
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginData
import com.example.hampouch.data.remote.dto.SocialLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    /**
     * 소셜 회원가입 온보딩 마지막 단계: 신규 유저의 최초 닉네임을 설정한다.
     * 이미 닉네임이 설정된 계정으로 다시 호출하면 서버가 409(USER_NICKNAME_ALREADY_SET)를 내려준다.
     */
    @PATCH("api/auth/nickname")
    suspend fun setNickname(
        @Header("Authorization") authorization: String,
        @Body request: SetNicknameRequest
    ): Response<ApiResponse<SetNicknameData>>

    @GET("api/auth/me")
    suspend fun getMe(
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<AuthMeData>>

    /**
     * date를 생략하면 서버가 오늘 날짜로 조회한다. 형식은 yyyy-MM-dd.
     */
    @GET("api/mini-challenges")
    suspend fun getMiniChallenges(
        @Header("Authorization") authorization: String,
        @Query("date") date: String? = null
    ): Response<ApiResponse<MiniChallengeDayData>>

    /**
     * durationDays를 생략하면 전체 기간의 추천 목록을 돌려준다.
     */
    @GET("api/mini-challenges/recommended")
    suspend fun getRecommendedMiniChallenges(
        @Header("Authorization") authorization: String,
        @Query("durationDays") durationDays: Int? = null
    ): Response<ApiResponse<RecommendedMiniChallengeListData>>

    @POST("api/mini-challenges")
    suspend fun addRecommendedMiniChallenge(
        @Header("Authorization") authorization: String,
        @Body request: AddRecommendedMiniChallengeRequest
    ): Response<ApiResponse<MiniChallengeCreatedData>>

    @POST("api/mini-challenges")
    suspend fun addCustomMiniChallenge(
        @Header("Authorization") authorization: String,
        @Body request: AddCustomMiniChallengeRequest
    ): Response<ApiResponse<MiniChallengeCreatedData>>

    @DELETE("api/mini-challenges/{miniChallengeId}")
    suspend fun deleteMiniChallenge(
        @Header("Authorization") authorization: String,
        @Path("miniChallengeId") miniChallengeId: Long
    ): Response<Unit>

    @PUT("api/mini-challenges/{miniChallengeId}/check")
    suspend fun checkMiniChallenge(
        @Header("Authorization") authorization: String,
        @Path("miniChallengeId") miniChallengeId: Long,
        @Body request: MiniChallengeCheckRequest
    ): Response<ApiResponse<MiniChallengeCheckData>>
}
