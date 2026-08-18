package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.AddCustomMiniChallengeRequest
import com.example.hampouch.data.remote.dto.AddRecommendedMiniChallengeRequest
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.AuthMeData
import com.example.hampouch.data.remote.dto.BattleDetailData
import com.example.hampouch.data.remote.dto.BattleInvitationPreviewData
import com.example.hampouch.data.remote.dto.ChallengeAdjustData
import com.example.hampouch.data.remote.dto.ChallengeAdjustRequest
import com.example.hampouch.data.remote.dto.ChallengeCalendarData
import com.example.hampouch.data.remote.dto.ChallengeCloseData
import com.example.hampouch.data.remote.dto.ChallengeCreateData
import com.example.hampouch.data.remote.dto.ChallengeCreateRequest
import com.example.hampouch.data.remote.dto.ChallengeCurrentData
import com.example.hampouch.data.remote.dto.ChallengeDayData
import com.example.hampouch.data.remote.dto.ChallengeDayRequest
import com.example.hampouch.data.remote.dto.ChallengeFixedDateDraftData
import com.example.hampouch.data.remote.dto.ChallengeFixedDateStartData
import com.example.hampouch.data.remote.dto.ChallengeFixedDateStartRequest
import com.example.hampouch.data.remote.dto.ChallengeFocusCategoriesData
import com.example.hampouch.data.remote.dto.ChallengeFocusCategoriesRequest
import com.example.hampouch.data.remote.dto.ChallengeHistoryListData
import com.example.hampouch.data.remote.dto.ChallengeRecommendationData
import com.example.hampouch.data.remote.dto.ChallengeResultData
import com.example.hampouch.data.remote.dto.ChallengeStatusData
import com.example.hampouch.data.remote.dto.CommunityBookmarkToggleData
import com.example.hampouch.data.remote.dto.ExpenseAnalysisData
import com.example.hampouch.data.remote.dto.ExpenseCategoryAnalysisData
import com.example.hampouch.data.remote.dto.ExpenseCreateRequest
import com.example.hampouch.data.remote.dto.ExpenseDaySummaryData
import com.example.hampouch.data.remote.dto.ExpenseDetailData
import com.example.hampouch.data.remote.dto.ExpenseEmotionAnalysisData
import com.example.hampouch.data.remote.dto.ExpenseIdData
import com.example.hampouch.data.remote.dto.ExpenseNoSpendRequest
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
import com.example.hampouch.data.remote.dto.LogoutRequest
import com.example.hampouch.data.remote.dto.MyBattlesData
import com.example.hampouch.data.remote.dto.MiniChallengeCheckData
import com.example.hampouch.data.remote.dto.MiniChallengeCheckRequest
import com.example.hampouch.data.remote.dto.MiniChallengeCreatedData
import com.example.hampouch.data.remote.dto.MiniChallengeDayData
import com.example.hampouch.data.remote.dto.DeviceTokenRequest
import com.example.hampouch.data.remote.dto.NicknameCheckData
import com.example.hampouch.data.remote.dto.NotificationListData
import com.example.hampouch.data.remote.dto.PasswordResetRequest
import com.example.hampouch.data.remote.dto.RecommendedMiniChallengeListData
import com.example.hampouch.data.remote.dto.RefreshTokenData
import com.example.hampouch.data.remote.dto.RefreshTokenRequest
import com.example.hampouch.data.remote.dto.RestResumeData
import com.example.hampouch.data.remote.dto.RestResumeRequest
import com.example.hampouch.data.remote.dto.RestStartData
import com.example.hampouch.data.remote.dto.RestStartRequest
import com.example.hampouch.data.remote.dto.SetNicknameData
import com.example.hampouch.data.remote.dto.SetNicknameRequest
import com.example.hampouch.data.remote.dto.SignUpData
import com.example.hampouch.data.remote.dto.SignUpRequest
import com.example.hampouch.data.remote.dto.SocialLoginData
import com.example.hampouch.data.remote.dto.SocialLoginRequest
import com.example.hampouch.data.remote.dto.UsersMeData
import com.example.hampouch.data.remote.dto.UsersNicknameUpdateData
import com.example.hampouch.data.remote.dto.UsersNicknameUpdateRequest
import com.example.hampouch.data.remote.dto.UsersNotificationScheduleData
import com.example.hampouch.data.remote.dto.UsersNotificationScheduleRequest
import com.example.hampouch.data.remote.dto.UsersPasswordChangeRequest
import com.example.hampouch.data.remote.dto.UsersProfilePhotoApplyData
import com.example.hampouch.data.remote.dto.UsersProfilePhotoApplyRequest
import com.example.hampouch.data.remote.dto.UsersProfilePhotoPresignData
import com.example.hampouch.data.remote.dto.UsersProfilePhotoPresignRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Tag
import com.example.hampouch.core.network.PendingAuth
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

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
        @Tag pendingAuth: PendingAuth,
        @Body request: SetNicknameRequest
    ): Response<ApiResponse<SetNicknameData>>

    @GET("api/auth/me")
    suspend fun getMe(@Tag pendingAuth: PendingAuth? = null): Response<ApiResponse<AuthMeData>>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<RefreshTokenData>>

    @POST("api/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/auth/me")
    suspend fun withdraw(): Response<ApiResponse<Unit>>
}

interface BattleApi {
    /**
     * 로그인한 사용자가 참가 중인 햄배틀을 상태별로 조회한다. status를 생략하면 전체 상태를 조회한다.
     */
    @GET("api/battles")
    suspend fun getMyBattles(
        @Query("status") status: String? = null
    ): Response<ApiResponse<MyBattlesData>>

    @POST("api/battles")
    suspend fun createBattle(
        @Body request: CreateBattleRequest
    ): Response<ApiResponse<CreateBattleData>>

    @GET("api/battles/invitations/{battleCode}")
    suspend fun getBattleInvitation(
        @Path("battleCode") battleCode: String
    ): Response<ApiResponse<BattleInvitationPreviewData>>

    @POST("api/battles/invitations/{battleCode}")
    suspend fun joinBattle(
        @Path("battleCode") battleCode: String
    ): Response<ApiResponse<JoinBattleData>>

    @GET("api/battles/{battleId}")
    suspend fun getBattleDetail(
        @Path("battleId") battleId: Long
    ): Response<ApiResponse<BattleDetailData>>
}

interface MiniChallengeApi {
    @GET("api/mini-challenges")
    suspend fun getMiniChallenges(
        @Query("date") date: String? = null
    ): Response<ApiResponse<MiniChallengeDayData>>

    @GET("api/mini-challenges/recommended")
    suspend fun getRecommendedMiniChallenges(
        @Query("durationDays") durationDays: Int? = null
    ): Response<ApiResponse<RecommendedMiniChallengeListData>>

    @POST("api/mini-challenges")
    suspend fun addRecommendedMiniChallenge(
        @Body request: AddRecommendedMiniChallengeRequest
    ): Response<ApiResponse<MiniChallengeCreatedData>>

    @POST("api/mini-challenges")
    suspend fun addCustomMiniChallenge(
        @Body request: AddCustomMiniChallengeRequest
    ): Response<ApiResponse<MiniChallengeCreatedData>>

    @DELETE("api/mini-challenges/{miniChallengeId}")
    suspend fun deleteMiniChallenge(
        @Path("miniChallengeId") miniChallengeId: Long
    ): Response<Unit>

    @PUT("api/mini-challenges/{miniChallengeId}/check")
    suspend fun checkMiniChallenge(
        @Path("miniChallengeId") miniChallengeId: Long,
        @Body request: MiniChallengeCheckRequest
    ): Response<ApiResponse<MiniChallengeCheckData>>
}

interface CommunityApi {
    @GET("api/community/home")
    suspend fun getCommunityHome(
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityHomeData>>

    @GET("api/community/posts")
    suspend fun getCommunityPosts(
        @Query("category") category: String,
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/posts/{postId}")
    suspend fun getCommunityPostDetail(
        @Path("postId") postId: Long,
        @Query("commentPage") commentPage: Int,
        @Query("commentSize") commentSize: Int
    ): Response<ApiResponse<CommunityPostDetailData>>

    @DELETE("api/community/posts/{postId}")
    suspend fun deleteCommunityPost(
        @Path("postId") postId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/community/posts/tips")
    suspend fun createCommunityTipPost(
        @Body request: CommunityTipWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @PATCH("api/community/posts/tips/{postId}")
    suspend fun updateCommunityTipPost(
        @Path("postId") postId: Long,
        @Body request: CommunityTipWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @POST("api/community/posts/foods")
    suspend fun createCommunityFoodPost(
        @Body request: CommunityFoodWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @PATCH("api/community/posts/foods/{postId}")
    suspend fun updateCommunityFoodPost(
        @Path("postId") postId: Long,
        @Body request: CommunityFoodWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @POST("api/community/posts/recruits")
    suspend fun createCommunityRecruitPost(
        @Body request: CommunityRecruitWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @PATCH("api/community/posts/recruits/{postId}")
    suspend fun updateCommunityRecruitPost(
        @Path("postId") postId: Long,
        @Body request: CommunityRecruitWriteRequest
    ): Response<ApiResponse<CommunityPostIdData>>

    @POST("api/community/posts/{postId}/comments")
    suspend fun createCommunityComment(
        @Path("postId") postId: Long,
        @Body request: CommunityCommentWriteRequest
    ): Response<ApiResponse<CommunityCommentWriteData>>

    @DELETE("api/community/comments/{commentId}")
    suspend fun deleteCommunityComment(
        @Path("commentId") commentId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/community/posts/{postId}/likes")
    suspend fun toggleCommunityLike(
        @Path("postId") postId: Long
    ): Response<ApiResponse<CommunityLikeToggleData>>

    @POST("api/community/posts/{postId}/bookmarks")
    suspend fun toggleCommunityBookmark(
        @Path("postId") postId: Long
    ): Response<ApiResponse<CommunityBookmarkToggleData>>

    @GET("api/community/me/posts")
    suspend fun getMyCommunityPosts(
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/me/bookmarks")
    suspend fun getMyCommunityBookmarks(
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/posts/popular")
    suspend fun getCommunityPopularPosts(
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @GET("api/community/posts/pochi-picks")
    suspend fun getCommunityPochiPicks(
        @Query("sortType") sortType: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<CommunityPostPageData<CommunityPostSummaryData>>>

    @POST("api/community/images/presign")
    suspend fun presignCommunityImages(
        @Body request: CommunityImagePresignRequest
    ): Response<ApiResponse<CommunityImagePresignData>>
}

interface ExpenseApi {
    @POST("api/expenses")
    suspend fun createExpense(
        @Body request: ExpenseCreateRequest
    ): Response<ApiResponse<ExpenseIdData>>

    @PUT("api/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("expenseId") expenseId: Long,
        @Body request: ExpenseUpdateRequest
    ): Response<ApiResponse<ExpenseIdData>>

    @GET("api/expenses/{expenseId}")
    suspend fun getExpenseDetail(
        @Path("expenseId") expenseId: Long
    ): Response<ApiResponse<ExpenseDetailData>>

    @DELETE("api/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("expenseId") expenseId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/expenses/photos/presigned")
    suspend fun presignExpensePhoto(
        @Query("expenseId") expenseId: Long?,
        @Body request: ExpensePhotoPresignRequest
    ): Response<ApiResponse<ExpensePhotoPresignData>>

    @PATCH("api/expenses/{expenseId}/photos")
    suspend fun confirmExpensePhoto(
        @Path("expenseId") expenseId: Long,
        @Body request: ExpensePhotoConfirmRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/expenses/{expenseId}/photos")
    suspend fun deleteExpensePhoto(
        @Path("expenseId") expenseId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * "오늘은 안 썼어요". 0원 지출을 만드는 게 아니라 no_spend_day 기록을 남긴다.
     * 같은 날짜에 일반 지출이 생기면 서버가 이 기록을 지운다.
     */
    @PUT("api/expenses/no-spend")
    suspend fun markNoSpend(
        @Body request: ExpenseNoSpendRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/expenses/day")
    suspend fun getExpenseDay(
        @Query("date") date: String
    ): Response<ApiResponse<ExpenseDaySummaryData>>

    @GET("api/expenses/summary/week")
    suspend fun getExpenseWeekSummary(
        @Query("standardDate") standardDate: String
    ): Response<ApiResponse<ExpensePeriodSummaryData>>

    @GET("api/expenses/summary/month")
    suspend fun getExpenseMonthSummary(
        @Query("standardMonth") standardMonth: String
    ): Response<ApiResponse<ExpensePeriodSummaryData>>

    @GET("api/expenses/analysis")
    suspend fun getExpenseAnalysis(
        @Query("periodStart") periodStart: String,
        @Query("periodEnd") periodEnd: String
    ): Response<ApiResponse<ExpenseAnalysisData>>

    @GET("api/expenses/analysis/category/{category}")
    suspend fun getExpenseCategoryAnalysis(
        @Path("category") category: String,
        @Query("periodStart") periodStart: String,
        @Query("periodEnd") periodEnd: String
    ): Response<ApiResponse<ExpenseCategoryAnalysisData>>

    @GET("api/expenses/analysis/emotion/{emotion}")
    suspend fun getExpenseEmotionAnalysis(
        @Path("emotion") emotion: String,
        @Query("periodStart") periodStart: String,
        @Query("periodEnd") periodEnd: String
    ): Response<ApiResponse<ExpenseEmotionAnalysisData>>

    @GET("api/expenses/analysis/trend")
    suspend fun getExpenseTrend(
        @Query("month") month: String
    ): Response<ApiResponse<ExpenseTrendData>>
}

interface ChallengeApi {
    @POST("api/challenges")
    suspend fun createChallenge(
        @Body request: ChallengeCreateRequest
    ): Response<ApiResponse<ChallengeCreateData>>

    @GET("api/challenges/current")
    suspend fun getCurrentChallenge(
    ): Response<ApiResponse<ChallengeCurrentData>>

    @GET("api/challenges/history")
    suspend fun getChallengeHistory(
    ): Response<ApiResponse<ChallengeHistoryListData>>

    @GET("api/challenges/{challengeId}/result")
    suspend fun getChallengeResult(
        @Path("challengeId") challengeId: Long
    ): Response<ApiResponse<ChallengeResultData>>

    @POST("api/challenges/{challengeId}/give-up")
    suspend fun giveUpChallenge(
        @Path("challengeId") challengeId: Long
    ): Response<ApiResponse<ChallengeStatusData>>

    @PUT("api/challenges/{challengeId}/focus-categories")
    suspend fun updateChallengeFocusCategories(
        @Path("challengeId") challengeId: Long,
        @Body request: ChallengeFocusCategoriesRequest
    ): Response<ApiResponse<ChallengeFocusCategoriesData>>

    @POST("api/challenges/{challengeId}/close")
    suspend fun closeChallenge(
        @Path("challengeId") challengeId: Long
    ): Response<ApiResponse<ChallengeCloseData>>

    @POST("api/challenges/fixed-date/start")
    suspend fun startFixedDateChallenge(
        @Body request: ChallengeFixedDateStartRequest
    ): Response<ApiResponse<ChallengeFixedDateStartData>>

    @GET("api/challenges/fixed-date/draft")
    suspend fun getFixedDateChallengeDraft(
    ): Response<ApiResponse<ChallengeFixedDateDraftData>>

    @GET("api/challenges/{challengeId}/calendar")
    suspend fun getChallengeCalendar(
        @Path("challengeId") challengeId: Long,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<ChallengeCalendarData>>

    @POST("api/challenges/{challengeId}/days")
    suspend fun recordChallengeDay(
        @Path("challengeId") challengeId: Long,
        @Body request: ChallengeDayRequest
    ): Response<ApiResponse<ChallengeDayData>>

    @POST("api/challenges/{challengeId}/adjust")
    suspend fun adjustChallengeBudget(
        @Path("challengeId") challengeId: Long,
        @Body request: ChallengeAdjustRequest
    ): Response<ApiResponse<ChallengeAdjustData>>

    @GET("api/challenges/recommendation")
    suspend fun getChallengeRecommendation(
    ): Response<ApiResponse<ChallengeRecommendationData>>
}

interface RestApi {
    @POST("api/rests")
    suspend fun startRest(
        @Body request: RestStartRequest
    ): Response<ApiResponse<RestStartData>>

    @POST("api/rests/resume")
    suspend fun resumeRest(
        @Body request: RestResumeRequest
    ): Response<ApiResponse<RestResumeData>>
}

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getNotifications(): Response<ApiResponse<NotificationListData>>

    @PATCH("api/notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Long
    ): Response<ApiResponse<Unit>>

    @PATCH("api/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiResponse<Unit>>

    @POST("api/notifications/token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<ApiResponse<Unit>>

    @DELETE("api/notifications/token")
    suspend fun unregisterDeviceToken(@Body request: DeviceTokenRequest): Response<ApiResponse<Unit>>
}

interface UsersApi {
    @GET("api/users/me")
    suspend fun getMe(): Response<ApiResponse<UsersMeData>>

    @PATCH("api/users/me/nickname")
    suspend fun updateNickname(
        @Body request: UsersNicknameUpdateRequest
    ): Response<ApiResponse<UsersNicknameUpdateData>>

    @PATCH("api/users/me/password")
    suspend fun changePassword(
        @Body request: UsersPasswordChangeRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/users/me/notification/schedule")
    suspend fun getNotificationSchedule(): Response<ApiResponse<UsersNotificationScheduleData>>

    @PUT("api/users/me/notification/schedule")
    suspend fun updateNotificationSchedule(
        @Body request: UsersNotificationScheduleRequest
    ): Response<ApiResponse<UsersNotificationScheduleData>>

    @POST("api/users/me/profile/presigned")
    suspend fun presignProfilePhoto(
        @Body request: UsersProfilePhotoPresignRequest
    ): Response<ApiResponse<UsersProfilePhotoPresignData>>

    @PATCH("api/users/me/profile")
    suspend fun applyProfilePhoto(
        @Body request: UsersProfilePhotoApplyRequest
    ): Response<ApiResponse<UsersProfilePhotoApplyData>>

    @DELETE("api/users/me/profile")
    suspend fun resetProfilePhoto(): Response<ApiResponse<Unit>>
}
