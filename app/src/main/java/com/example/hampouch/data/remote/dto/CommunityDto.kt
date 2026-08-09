package com.example.hampouch.data.remote.dto

data class CommunityPostPageData<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
)

data class CommunityPostSummaryData(
    val postId: Long,
    val postType: String,
    val category: String,
    val title: String,
    val content: String,
    val thumbnailUrl: String?,
    val authorName: String,
    val createdAt: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val isMine: Boolean,
    val bookmarkedAt: String? = null
)

data class CommunityHomeData(
    val popularPosts: List<CommunityPostSummaryData>,
    val pochiPicks: List<CommunityPostSummaryData>,
    val posts: CommunityPostPageData<CommunityPostSummaryData>
)

data class CommunityAuthorData(
    val userId: Long,
    val authorName: String,
    val profileImageUrl: String?
)

data class CommunityImageData(
    val imageKey: String,
    val imageUrl: String
)

data class CommunityFoodDetailData(
    val menuName: String,
    val placeName: String,
    val price: Int,
    val tasteRating: Int,
    val costRating: Int,
    val moodRating: Int
)

data class CommunityRecruitDetailData(
    val battleId: Long,
    val battleUrl: String,
    val battleTitle: String,
    val startDate: String,
    val durationDays: Int,
    val maxMemberCount: Int,
    val currentMemberCount: Int,
    val penalty: String,
    val recruit: Boolean
)

data class CommunityCommentData(
    val commentId: Long,
    val userId: Long,
    val authorName: String,
    val profileImageUrl: String?,
    val content: String,
    val isDeleted: Boolean,
    val isMine: Boolean,
    val createdAt: String,
    val replies: List<CommunityCommentData> = emptyList()
)

data class CommunityPostDetailData(
    val postId: Long,
    val postType: String,
    val category: String,
    val title: String,
    val content: String,
    val author: CommunityAuthorData,
    val foodDetail: CommunityFoodDetailData? = null,
    val recruitDetail: CommunityRecruitDetailData? = null,
    val images: List<CommunityImageData>,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val isMine: Boolean,
    val comments: List<CommunityCommentData>
)

data class CommunityPostIdData(val postId: Long)

data class CommunityTipWriteRequest(
    val category: String,
    val title: String,
    val content: String,
    val imageKeys: List<String>
)

data class CommunityFoodWriteRequest(
    val title: String,
    val menuName: String,
    val placeName: String,
    val price: Int,
    val tasteRating: Int,
    val costRating: Int,
    val moodRating: Int,
    val content: String,
    val imageKeys: List<String>
)

data class CommunityRecruitWriteRequest(
    val title: String,
    val content: String,
    val battleUrl: String
)

data class CommunityCommentWriteRequest(
    val parentCommentId: Long?,
    val content: String
)

data class CommunityCommentWriteData(
    val commentId: Long,
    val postId: Long,
    val parentCommentId: Long?,
    val content: String,
    val createdAt: String
)

data class CommunityLikeToggleData(
    val postId: Long,
    val isLiked: Boolean,
    val likeCount: Int
)

data class CommunityBookmarkToggleData(
    val postId: Long,
    val isBookmarked: Boolean
)

data class CommunityImagePresignFileRequest(
    val contentType: String,
    val size: Long
)

data class CommunityImagePresignRequest(
    val files: List<CommunityImagePresignFileRequest>
)

data class CommunityImagePresignFileData(
    val uploadUrl: String,
    val imageKey: String,
    val imageUrl: String
)

data class CommunityImagePresignData(
    val files: List<CommunityImagePresignFileData>
)
