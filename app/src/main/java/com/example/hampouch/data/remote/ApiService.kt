package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.AddCustomMiniChallengeRequest
import com.example.hampouch.data.remote.dto.AddRecommendedMiniChallengeRequest
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.AuthMeData
import com.example.hampouch.data.remote.dto.BattleDetailData
import com.example.hampouch.data.remote.dto.BattleInvitationPreviewData
import com.example.hampouch.data.remote.dto.CommunityBookmarkToggleData
import com.example.hampouch.data.remote.dto.ExpenseAnalysisData
import com.example.hampouch.data.remote.dto.ExpenseCategoryAnalysisData
import com.example.hampouch.data.remote.dto.ExpenseCreateRequest
import com.example.hampouch.data.remote.dto.ExpenseDaySummaryData
import com.example.hampouch.data.remote.dto.ExpenseDetailData
import com.example.hampouch.data.remote.dto.ExpenseEmotionAnalysisData
import com.example.hampouch.data.remote.dto.ExpenseIdData
import com.example.hampouch.data.remote.dto.ExpensePeriodSummaryData
import com.example.hampouch.data.remote.dto.ExpensePhotoConfirmRequest
import com.example.hampouch.data.remote.dto.ExpensePhotoPresignData
import com.example.hampouch.data.remote.dto.ExpensePhotoPresignRequest
import com.example.hampouch.data.remote.dto.ExpenseTrendData
import com.example.hampouch.data.remote.dto.ExpenseUpdateRequest
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
import com.example.hampouch.data.remote.dto.CreateBattleData
import com.example.hampouch.data.remote.dto.CreateBattleRequest
import com.example.hampouch.data.remote.dto.EmailSendData
import com.example.hampouch.data.remote.dto.EmailSendRequest
import com.example.hampouch.data.remote.dto.EmailVerifyData
import com.example.hampouch.data.remote.dto.EmailVerifyRequest
import com.example.hampouch.data.remote.dto.JoinBattleData
import com.example.hampouch.data.remote.dto.LoginData
import com.example.hampouch.data.remote.dto.LoginRequest
import com.example.hampouch.data.remote.dto.MyBattlesData
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
     * 로그인한 사용자가 참가 중인 햄배틀을 상태별로 조회한다. status를 생략하면 전체 상태를 조회한다.
     */
    @GET("api/battles")
    suspend fun getMyBattles(
        @Header("Authorization") authorization: String,
        @Query("status") status: String? = null
    ): Response<ApiResponse<MyBattlesData>>

    @POST("api/battles")
    suspend fun createBattle(
        @Header("Authorization") authorization: String,
        @Body request: CreateBattleRequest
    ): Response<ApiResponse<CreateBattleData>>

    /**
     * battleCode로 참가 전 미리보기를 조회한다. battleId는 참가자 전용 리소스라 응답에 포함되지 않는다.
     */
    @GET("api/battles/invitations/{battleCode}")
    suspend fun getBattleInvitation(
        @Header("Authorization") authorization: String,
        @Path("battleCode") battleCode: String
    ): Response<ApiResponse<BattleInvitationPreviewData>>

    @POST("api/battles/invitations/{battleCode}")
    suspend fun joinBattle(
        @Header("Authorization") authorization: String,
        @Path("battleCode") battleCode: String
    ): Response<ApiResponse<JoinBattleData>>

    /**
     * 참가자 전용 상세 조회. READY/ONGOING/TERMINATED 모두 이 응답 하나로 표현된다.
     */
    @GET("api/battles/{battleId}")
    suspend fun getBattleDetail(
        @Header("Authorization") authorization: String,
        @Path("battleId") battleId: Long
    ): Response<ApiResponse<BattleDetailData>>

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

    @POST("api/expenses")
    suspend fun createExpense(
        @Header("Authorization") authorization: String,
        @Body request: ExpenseCreateRequest
    ): Response<ApiResponse<ExpenseIdData>>

    @PUT("api/expenses/{expenseId}")
    suspend fun updateExpense(
        @Header("Authorization") authorization: String,
        @Path("expenseId") expenseId: Long,
        @Body request: ExpenseUpdateRequest
    ): Response<ApiResponse<ExpenseIdData>>

    @GET("api/expenses/{expenseId}")
    suspend fun getExpenseDetail(
        @Header("Authorization") authorization: String,
        @Path("expenseId") expenseId: Long
    ): Response<ApiResponse<ExpenseDetailData>>

    @DELETE("api/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Header("Authorization") authorization: String,
        @Path("expenseId") expenseId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/expenses/photos/presigned")
    suspend fun presignExpensePhoto(
        @Header("Authorization") authorization: String,
        @Query("expenseId") expenseId: Long?,
        @Body request: ExpensePhotoPresignRequest
    ): Response<ApiResponse<ExpensePhotoPresignData>>

    @PATCH("api/expenses/{expenseId}/photos")
    suspend fun confirmExpensePhoto(
        @Header("Authorization") authorization: String,
        @Path("expenseId") expenseId: Long,
        @Body request: ExpensePhotoConfirmRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/expenses/{expenseId}/photos")
    suspend fun deleteExpensePhoto(
        @Header("Authorization") authorization: String,
        @Path("expenseId") expenseId: Long
    ): Response<ApiResponse<Unit>>

    @GET("api/expenses/day")
    suspend fun getExpenseDay(
        @Header("Authorization") authorization: String,
        @Query("date") date: String
    ): Response<ApiResponse<ExpenseDaySummaryData>>

    @GET("api/expenses/summary/week")
    suspend fun getExpenseWeekSummary(
        @Header("Authorization") authorization: String,
        @Query("standardDate") standardDate: String
    ): Response<ApiResponse<ExpensePeriodSummaryData>>

    @GET("api/expenses/summary/month")
    suspend fun getExpenseMonthSummary(
        @Header("Authorization") authorization: String,
        @Query("standardMonth") standardMonth: String
    ): Response<ApiResponse<ExpensePeriodSummaryData>>

    @GET("api/expenses/analysis")
    suspend fun getExpenseAnalysis(
        @Header("Authorization") authorization: String,
        @Query("periodStart") periodStart: String,
        @Query("periodEnd") periodEnd: String
    ): Response<ApiResponse<ExpenseAnalysisData>>

    @GET("api/expenses/analysis/category/{category}")
    suspend fun getExpenseCategoryAnalysis(
        @Header("Authorization") authorization: String,
        @Path("category") category: String,
        @Query("periodStart") periodStart: String,
        @Query("periodEnd") periodEnd: String
    ): Response<ApiResponse<ExpenseCategoryAnalysisData>>

    @GET("api/expenses/analysis/emotion/{emotion}")
    suspend fun getExpenseEmotionAnalysis(
        @Header("Authorization") authorization: String,
        @Path("emotion") emotion: String,
        @Query("periodStart") periodStart: String,
        @Query("periodEnd") periodEnd: String
    ): Response<ApiResponse<ExpenseEmotionAnalysisData>>

    @GET("api/expenses/analysis/trend")
    suspend fun getExpenseTrend(
        @Header("Authorization") authorization: String,
        @Query("month") month: String
    ): Response<ApiResponse<ExpenseTrendData>>
}
