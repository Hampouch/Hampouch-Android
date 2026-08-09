package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.CommunityBookmarkToggleData
import com.example.hampouch.data.remote.dto.CommunityCommentWriteData
import com.example.hampouch.data.remote.dto.CommunityCommentWriteRequest
import com.example.hampouch.data.remote.dto.CommunityFoodWriteRequest
import com.example.hampouch.data.remote.dto.CommunityHomeData
import com.example.hampouch.data.remote.dto.CommunityImagePresignData
import com.example.hampouch.data.remote.dto.CommunityImagePresignRequest
import com.example.hampouch.data.remote.dto.CommunityLikeToggleData
import com.example.hampouch.data.remote.dto.CommunityPostDetailData
import com.example.hampouch.data.remote.dto.CommunityPostIdData
import com.example.hampouch.data.remote.dto.CommunityPostPageData
import com.example.hampouch.data.remote.dto.CommunityPostSummaryData
import com.example.hampouch.data.remote.dto.CommunityRecruitWriteRequest
import com.example.hampouch.data.remote.dto.CommunityTipWriteRequest
import com.example.hampouch.data.remote.dto.EmailSendData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.data.remote.dto.EmailVerifyData
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.LoginData
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.NicknameCheckData
import com.example.hampouch.data.remote.dto.PasswordResetRequest
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

    @PATCH("api/auth/nickname")
    suspend fun setNickname(
        @Header("Authorization") authorization: String,
        @Body request: SetNicknameRequest
    ): Response<ApiResponse<SetNicknameData>>

    @GET("api/community/home")
    suspend fun getCommunityHome(
        @Header("Authorization") authorization: String,
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityHomeData>>

    @GET("api/community/posts")
    suspend fun getCommunityPosts(
        @Header("Authorization") authorization: String,
        @Query("category") category: String,
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/posts/{postId}")
    suspend fun getCommunityPostDetail(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long
    ): Response<ApiResponse<CommunityPostDetailData>>

    @DELETE("api/community/posts/{postId}")
    suspend fun deleteCommunityPost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/community/posts/tips")
    suspend fun createCommunityTipPost(
        @Header("Authorization") authorization: String,
        @Body request: CommunityTipWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @PATCH("api/community/posts/tips/{postId}")
    suspend fun updateCommunityTipPost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long,
        @Body request: CommunityTipWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @POST("api/community/posts/foods")
    suspend fun createCommunityFoodPost(
        @Header("Authorization") authorization: String,
        @Body request: CommunityFoodWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @PATCH("api/community/posts/foods/{postId}")
    suspend fun updateCommunityFoodPost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long,
        @Body request: CommunityFoodWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @POST("api/community/posts/recruits")
    suspend fun createCommunityRecruitPost(
        @Header("Authorization") authorization: String,
        @Body request: CommunityRecruitWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @PATCH("api/community/posts/recruits/{postId}")
    suspend fun updateCommunityRecruitPost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long,
        @Body request: CommunityRecruitWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @POST("api/community/posts/{postId}/comments")
    suspend fun createCommunityComment(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long,
        @Body request: CommunityCommentWriteRequest
    ): Response<ApiResponse<CommunityCommentWriteData>>

    @DELETE("api/community/comments/{commentId}")
    suspend fun deleteCommunityComment(
        @Header("Authorization") authorization: String,
        @Path("commentId") commentId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/community/posts/{postId}/likes")
    suspend fun toggleCommunityLike(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long
    ): Response<ApiResponse<CommunityLikeToggleData>>

    @POST("api/community/posts/{postId}/bookmarks")
    suspend fun toggleCommunityBookmark(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: Long
    ): Response<ApiResponse<CommunityBookmarkToggleData>>

    @GET("api/community/me/posts")
    suspend fun getMyCommunityPosts(
        @Header("Authorization") authorization: String,
        @Query("sortType") sortType: String,
        @Query("cursor") cursor: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/me/bookmarks")
    suspend fun getMyCommunityBookmarks(
        @Header("Authorization") authorization: String,
        @Query("sortType") sortType: String,
        @Query("cursor") cursor: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/posts/popular")
    suspend fun getCommunityPopularPosts(
        @Header("Authorization") authorization: String,
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/posts/pochi-picks")
    suspend fun getCommunityPochiPicks(
        @Header("Authorization") authorization: String,
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @POST("api/community/images/presign")
    suspend fun presignCommunityImages(
        @Header("Authorization") authorization: String,
        @Body request: CommunityImagePresignRequest
    ): Response<ApiResponse<CommunityImagePresignData>>
}
