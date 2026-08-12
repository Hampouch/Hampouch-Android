package com.example.hampouch.data.repository

import android.net.Uri
import android.util.Log
import com.example.hampouch.core.config.CommunityConfig
import com.example.hampouch.domain.model.BattleRecruitInfo
import com.example.hampouch.domain.model.HamTipsSortOrder
import com.example.hampouch.domain.model.MenuRatingInfo
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.TipComment
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.hampouch.data.local.HamTipsMockDataSource
import com.example.hampouch.data.remote.ApiService
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.HamTipsRepository
import okhttp3.OkHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipPostType
import com.example.hampouch.domain.model.TipReply
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.CommunityCommentData
import com.example.hampouch.data.remote.dto.CommunityCommentWriteRequest
import com.example.hampouch.data.remote.dto.CommunityFoodWriteRequest
import com.example.hampouch.data.remote.dto.CommunityImagePresignFileData
import com.example.hampouch.data.remote.dto.CommunityImagePresignFileRequest
import com.example.hampouch.data.remote.dto.CommunityImagePresignRequest
import com.example.hampouch.data.remote.dto.CommunityPostDetailData
import com.example.hampouch.data.remote.dto.CommunityPostSummaryData
import com.example.hampouch.data.remote.dto.CommunityRecruitWriteRequest
import com.example.hampouch.data.remote.dto.CommunityTipWriteRequest
import com.example.hampouch.domain.model.formatTimeAgoLabel
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private const val TAG = "HamTipsRepository"

private const val DEFAULT_BATTLE_DURATION_DAYS = 7
private const val DEFAULT_BATTLE_CAPACITY = 5
private const val JUST_NOW_LABEL = "방금"
private const val DEFAULT_PAGE_SIZE = 20

@Singleton
class HamTipsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient,
    private val authRepository: AuthRepository,
    private val mockDataSource: HamTipsMockDataSource
) : HamTipsRepository, AccountScopedState {

    private val activeUserId: String get() = authRepository.currentUser.value.id
    private val activeUserName: String get() = authRepository.currentUser.value.name

    private val _posts = MutableStateFlow(mockDataSource.allPosts())
    override val posts: StateFlow<List<TipPost>> = _posts.asStateFlow()

    private var nextId = 1000

    override fun postById(id: String): TipPost? = _posts.value.find { it.id == id }

    private fun newId(prefix: String): String = "${prefix}_${nextId++}"

    private fun mutate(postId: String, transform: (TipPost) -> TipPost) {
        _posts.update { list -> list.map { if (it.id == postId) transform(it) else it } }
    }

    private fun upsert(post: TipPost) {
        _posts.update { list ->
            if (list.any { it.id == post.id }) list.map { if (it.id == post.id) post else it } else list + post
        }
    }

    private fun prepend(post: TipPost) {
        _posts.update { listOf(post) + it.filterNot { existing -> existing.id == post.id } }
    }

    private fun removeById(postId: String) {
        _posts.update { list -> list.filterNot { it.id == postId } }
    }


    private fun HamTipsSortOrder.toServerSortType(): String = when (this) {
        HamTipsSortOrder.LATEST -> "LATEST"
        HamTipsSortOrder.POPULAR -> "POPULAR"
        HamTipsSortOrder.VIEWS -> "VIEW"
    }

    private fun TipCategory.toServerCategory(): String = when (this) {
        TipCategory.WHAT_TO_EAT -> "FOOD_RECOMMEND"
        TipCategory.SHOPPING -> "GROCERY"
        TipCategory.COOKING -> "COOKING"
        TipCategory.DISCOUNT -> "DISCOUNT"
        TipCategory.RECRUIT -> "RECRUIT"
        TipCategory.RECORD -> "RECORD"
        TipCategory.ETC -> "ETC"
    }

    private fun mapServerCategory(serverCategory: String): TipCategory = when (serverCategory) {
        "FOOD_RECOMMEND" -> TipCategory.WHAT_TO_EAT
        "GROCERY" -> TipCategory.SHOPPING
        "COOKING" -> TipCategory.COOKING
        "DISCOUNT" -> TipCategory.DISCOUNT
        "RECRUIT" -> TipCategory.RECRUIT
        "RECORD" -> TipCategory.RECORD
        else -> TipCategory.ETC
    }

    private fun mapServerPostType(serverPostType: String): TipPostType = when (serverPostType) {
        "FOOD_RECOMMEND" -> TipPostType.MENU
        "RECRUIT" -> TipPostType.BATTLE
        else -> TipPostType.TIP
    }

    private fun minutesAgoFrom(createdAt: String): Int = try {
        ChronoUnit.MINUTES.between(LocalDateTime.parse(createdAt), LocalDateTime.now()).toInt().coerceAtLeast(0)
    } catch (e: DateTimeParseException) {
        0
    }

    private fun CommunityPostSummaryData.toTipPost(isEditorAuthor: Boolean? = null): TipPost = TipPost(
        id = postId.toString(),
        type = mapServerPostType(postType),
        category = mapServerCategory(category),
        title = title,
        subtitle = content,
        content = content,
        authorId = if (isMine) activeUserId else "",
        authorName = authorName,
        isEditorAuthor = isEditorAuthor ?: (postById(postId.toString())?.isEditorAuthor ?: false),
        postedMinutesAgo = minutesAgoFrom(createdAt),
        viewCount = viewCount,
        commentCount = commentCount,
        likeCount = likeCount,
        hasImage = thumbnailUrl != null,
        imageUris = listOfNotNull(thumbnailUrl),
        isLiked = isLiked,
        isSaved = isBookmarked
    )

    private fun CommunityCommentData.toTipReply(): TipReply = TipReply(
        id = commentId.toString(),
        authorId = if (isMine) activeUserId else userId.toString(),
        authorName = authorName,
        content = content,
        timeLabel = formatTimeAgoLabel(minutesAgoFrom(createdAt)),
        isDeleted = isDeleted
    )

    private fun CommunityCommentData.toTipComment(): TipComment = TipComment(
        id = commentId.toString(),
        authorId = if (isMine) activeUserId else userId.toString(),
        authorName = authorName,
        content = content,
        timeLabel = formatTimeAgoLabel(minutesAgoFrom(createdAt)),
        isDeleted = isDeleted,
        replies = replies.map { it.toTipReply() }
    )

    private fun CommunityPostDetailData.toTipPost(isEditorAuthor: Boolean): TipPost = TipPost(
        id = postId.toString(),
        type = mapServerPostType(postType),
        category = mapServerCategory(category),
        title = title,
        subtitle = content,
        content = content,
        authorId = if (isMine) activeUserId else author.userId.toString(),
        authorName = author.authorName,
        isEditorAuthor = isEditorAuthor,
        postedMinutesAgo = minutesAgoFrom(createdAt),
        viewCount = viewCount,
        commentCount = commentCount,
        likeCount = likeCount,
        hasImage = images.isNotEmpty(),
        imageUris = images.map { it.imageUrl },
        imageKeys = images.map { it.imageKey },
        menuName = foodDetail?.menuName.orEmpty(),
        place = foodDetail?.placeName.orEmpty(),
        price = foodDetail?.price ?: 0,
        menuRating = foodDetail?.let {
            MenuRatingInfo(taste = it.tasteRating, costEffectiveness = it.costRating, mood = it.moodRating)
        },
        battleInfo = recruitDetail?.let {
            BattleRecruitInfo(
                link = it.battleUrl,
                durationDays = it.durationDays,
                capacity = it.maxMemberCount,
                penalty = it.penalty,
                currentMemberCount = it.currentMemberCount
            )
        },
        comments = comments.map { it.toTipComment() },
        isLiked = isLiked,
        isSaved = isBookmarked
    )


    private suspend fun requireAuthHeader(): Result<String> {
        val header = authRepository.currentAuthHeader()
        return if (header != null) {
            Result.success(header)
        } else {
            Result.failure(ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다."))
        }
    }

    private fun errorFrom(response: Response<*>, fallbackMessage: String): ApiException {
        val error = response.errorBody()?.string()?.let {
            runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
        }
        return ApiException(code = error?.code ?: "UNKNOWN", message = error?.message ?: fallbackMessage)
    }

    private inline fun <T> runCatchingNetwork(action: () -> Result<T>): Result<T> = try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        Log.e(TAG, "커뮤니티 API 오류", e)
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "커뮤니티 네트워크 오류", e)
        Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
    }


    override suspend fun loadHome(sortType: HamTipsSortOrder): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getCommunityHome(header, sortType.toServerSortType(), 0, DEFAULT_PAGE_SIZE)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.popularPosts.forEach { upsert(it.toTipPost()) }
                data.pochiPicks.forEach { upsert(it.toTipPost(isEditorAuthor = true)) }
                data.posts.content.forEach { upsert(it.toTipPost()) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "커뮤니티 홈을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadCategoryPosts(category: TipCategory, sortType: HamTipsSortOrder): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getCommunityPosts(
                header, category.toServerCategory(), sortType.toServerSortType(), 0, DEFAULT_PAGE_SIZE
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.content.forEach { upsert(it.toTipPost()) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "게시글 목록을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadPopularPosts(sortType: HamTipsSortOrder): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getCommunityPopularPosts(header, sortType.toServerSortType(), 0, DEFAULT_PAGE_SIZE)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.content.forEach { upsert(it.toTipPost()) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "인기글을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadPochipickPosts(sortType: HamTipsSortOrder): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getCommunityPochiPicks(header, sortType.toServerSortType(), 0, DEFAULT_PAGE_SIZE)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.content.forEach { upsert(it.toTipPost(isEditorAuthor = true)) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "포치픽 목록을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadMyPosts(sortType: HamTipsSortOrder): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getMyCommunityPosts(header, sortType.toServerSortType(), 0, DEFAULT_PAGE_SIZE)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.content.forEach { upsert(it.toTipPost()) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "내가 쓴 꿀팁을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadSavedPosts(sortType: HamTipsSortOrder): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getMyCommunityBookmarks(header, sortType.toServerSortType(), 0, DEFAULT_PAGE_SIZE)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.content.forEach { upsert(it.toTipPost()) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "저장한 꿀팁을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadPostDetail(postId: String): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { it.copy(viewCount = it.viewCount + 1) }
            val post = postById(postId)
            return if (post != null) Result.success(post) else Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        }
        val id = postId.toLongOrNull()
            ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.getCommunityPostDetail(header, id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val isEditorAuthor = postById(postId)?.isEditorAuthor ?: false
                val post = data.toTipPost(isEditorAuthor)
                upsert(post)
                Result.success(post)
            } else {
                Result.failure(errorFrom(response, "게시글을 불러오지 못했습니다."))
            }
        }
    }


    private data class LocalImagePayload(val bytes: ByteArray, val contentType: String)

    private suspend fun readLocalImage(uriString: String): LocalImagePayload = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ApiException("COMMUNITY_IMAGE_UPLOAD_FAILED", "이미지를 읽을 수 없습니다.")
        LocalImagePayload(bytes = bytes, contentType = resolver.getType(uri) ?: "image/jpeg")
    }

    private suspend fun uploadImages(localUris: List<String>): Result<List<CommunityImagePresignFileData>> {
        if (localUris.isEmpty()) return Result.success(emptyList())
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val payloads = localUris.map { readLocalImage(it) }
            val response = apiService.presignCommunityImages(
                header,
                CommunityImagePresignRequest(
                    files = payloads.map { CommunityImagePresignFileRequest(it.contentType, it.bytes.size.toLong()) }
                )
            )
            val data = response.body()?.data
            if (!response.isSuccessful || data == null) {
                return@runCatchingNetwork Result.failure(errorFrom(response, "이미지 업로드에 실패했습니다."))
            }
            withContext(Dispatchers.IO) {
                data.files.forEachIndexed { index, file ->
                    val body = payloads[index].bytes.toRequestBody(payloads[index].contentType.toMediaTypeOrNull())
                    val request = Request.Builder().url(file.uploadUrl).put(body).build()
                    okHttpClient.newCall(request).execute().use { httpResponse ->
                        if (!httpResponse.isSuccessful) {
                            throw ApiException("COMMUNITY_IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다.")
                        }
                    }
                }
            }
            Result.success(data.files)
        }
    }

    private suspend fun resolveImageKeys(imageUris: List<String>, imageKeys: List<String>): Result<List<String>> {
        val resolved = imageUris.indices.map { index -> imageKeys.getOrNull(index).orEmpty() }.toMutableList()
        val pendingIndices = resolved.indices.filter { resolved[it].isBlank() }
        if (pendingIndices.isEmpty()) return Result.success(resolved)
        val uploaded = uploadImages(pendingIndices.map { imageUris[it] }).getOrElse { return Result.failure(it) }
        pendingIndices.forEachIndexed { i, index -> resolved[index] = uploaded[i].imageKey }
        return Result.success(resolved)
    }


    override suspend fun createTipPost(
        category: TipCategory,
        title: String,
        content: String,
        imageUris: List<String>,
        imageKeys: List<String>
    ): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            val post = TipPost(
                id = newId("tip"),
                type = TipPostType.TIP,
                category = category,
                title = title,
                subtitle = content,
                content = content,
                authorId = activeUserId,
                authorName = activeUserName,
                isEditorAuthor = authRepository.isEditor,
                hasImage = imageUris.isNotEmpty(),
                imageUris = imageUris
            )
            prepend(post)
            return Result.success(post)
        }
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        val keys = resolveImageKeys(imageUris, imageKeys).getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.createCommunityTipPost(
                header, CommunityTipWriteRequest(category.toServerCategory(), title, content, keys)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val post = TipPost(
                    id = data.postId.toString(),
                    type = TipPostType.TIP,
                    category = category,
                    title = title,
                    subtitle = content,
                    content = content,
                    authorId = activeUserId,
                    authorName = activeUserName,
                    isEditorAuthor = authRepository.isEditor,
                    hasImage = imageUris.isNotEmpty(),
                    imageUris = imageUris,
                    imageKeys = keys
                )
                prepend(post)
                Result.success(post)
            } else {
                Result.failure(errorFrom(response, "꿀팁 작성에 실패했습니다."))
            }
        }
    }

    override suspend fun updateTipPost(
        postId: String,
        category: TipCategory,
        title: String,
        content: String,
        imageUris: List<String>,
        imageKeys: List<String>
    ): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                post.copy(
                    category = category,
                    title = title,
                    subtitle = content,
                    content = content,
                    hasImage = imageUris.isNotEmpty(),
                    imageUris = imageUris
                )
            }
            val post = postById(postId) ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
            return Result.success(post)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        val keys = resolveImageKeys(imageUris, imageKeys).getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.updateCommunityTipPost(
                header, id, CommunityTipWriteRequest(category.toServerCategory(), title, content, keys)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                mutate(postId) { post ->
                    post.copy(
                        category = category,
                        title = title,
                        subtitle = content,
                        content = content,
                        hasImage = imageUris.isNotEmpty(),
                        imageUris = imageUris,
                        imageKeys = keys
                    )
                }
                Result.success(postById(postId)!!)
            } else {
                Result.failure(errorFrom(response, "꿀팁 수정에 실패했습니다."))
            }
        }
    }

    override suspend fun createMenuPost(
        title: String,
        menuName: String,
        place: String,
        price: Int,
        rating: MenuRatingInfo,
        comment: String,
        imageUris: List<String>,
        imageKeys: List<String>
    ): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            val post = TipPost(
                id = newId("menu"),
                type = TipPostType.MENU,
                category = TipCategory.WHAT_TO_EAT,
                title = title,
                subtitle = comment,
                content = comment,
                authorId = activeUserId,
                authorName = activeUserName,
                isEditorAuthor = authRepository.isEditor,
                hasImage = imageUris.isNotEmpty(),
                imageUris = imageUris,
                menuName = menuName,
                place = place,
                price = price,
                menuRating = rating
            )
            prepend(post)
            return Result.success(post)
        }
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        val keys = resolveImageKeys(imageUris, imageKeys).getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.createCommunityFoodPost(
                header,
                CommunityFoodWriteRequest(
                    title = title, menuName = menuName, placeName = place, price = price,
                    tasteRating = rating.taste, costRating = rating.costEffectiveness, moodRating = rating.mood,
                    content = comment, imageKeys = keys
                )
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val post = TipPost(
                    id = data.postId.toString(),
                    type = TipPostType.MENU,
                    category = TipCategory.WHAT_TO_EAT,
                    title = title,
                    subtitle = comment,
                    content = comment,
                    authorId = activeUserId,
                    authorName = activeUserName,
                    isEditorAuthor = authRepository.isEditor,
                    hasImage = imageUris.isNotEmpty(),
                    imageUris = imageUris,
                    imageKeys = keys,
                    menuName = menuName,
                    place = place,
                    price = price,
                    menuRating = rating
                )
                prepend(post)
                Result.success(post)
            } else {
                Result.failure(errorFrom(response, "뭐먹지 글 작성에 실패했습니다."))
            }
        }
    }

    override suspend fun updateMenuPost(
        postId: String,
        title: String,
        menuName: String,
        place: String,
        price: Int,
        rating: MenuRatingInfo,
        comment: String,
        imageUris: List<String>,
        imageKeys: List<String>
    ): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                post.copy(
                    title = title, subtitle = comment, content = comment,
                    hasImage = imageUris.isNotEmpty(), imageUris = imageUris,
                    menuName = menuName, place = place, price = price, menuRating = rating
                )
            }
            val post = postById(postId) ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
            return Result.success(post)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        val keys = resolveImageKeys(imageUris, imageKeys).getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.updateCommunityFoodPost(
                header, id,
                CommunityFoodWriteRequest(
                    title = title, menuName = menuName, placeName = place, price = price,
                    tasteRating = rating.taste, costRating = rating.costEffectiveness, moodRating = rating.mood,
                    content = comment, imageKeys = keys
                )
            )
            if (response.isSuccessful && response.body()?.data != null) {
                mutate(postId) { post ->
                    post.copy(
                        title = title, subtitle = comment, content = comment,
                        hasImage = imageUris.isNotEmpty(), imageUris = imageUris, imageKeys = keys,
                        menuName = menuName, place = place, price = price, menuRating = rating
                    )
                }
                Result.success(postById(postId)!!)
            } else {
                Result.failure(errorFrom(response, "뭐먹지 글 수정에 실패했습니다."))
            }
        }
    }

    override suspend fun createBattlePost(title: String, content: String, link: String): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            val post = TipPost(
                id = newId("battle"),
                type = TipPostType.BATTLE,
                category = TipCategory.RECRUIT,
                title = title,
                subtitle = content,
                content = content,
                authorId = activeUserId,
                authorName = activeUserName,
                battleInfo = BattleRecruitInfo(
                    link = link,
                    durationDays = DEFAULT_BATTLE_DURATION_DAYS,
                    capacity = DEFAULT_BATTLE_CAPACITY,
                    penalty = ""
                )
            )
            prepend(post)
            return Result.success(post)
        }
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.createCommunityRecruitPost(
                header, CommunityRecruitWriteRequest(title = title, content = content, battleUrl = link)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val post = TipPost(
                    id = data.postId.toString(),
                    type = TipPostType.BATTLE,
                    category = TipCategory.RECRUIT,
                    title = title,
                    subtitle = content,
                    content = content,
                    authorId = activeUserId,
                    authorName = activeUserName,
                    battleInfo = BattleRecruitInfo(
                        link = link,
                        durationDays = DEFAULT_BATTLE_DURATION_DAYS,
                        capacity = DEFAULT_BATTLE_CAPACITY,
                        penalty = ""
                    )
                )
                prepend(post)
                Result.success(post)
            } else {
                Result.failure(errorFrom(response, "햄배틀 모집 글 작성에 실패했습니다."))
            }
        }
    }

    override suspend fun updateBattlePost(postId: String, title: String, content: String, link: String): Result<TipPost> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                post.copy(
                    title = title,
                    subtitle = content,
                    content = content,
                    battleInfo = post.battleInfo?.copy(link = link) ?: BattleRecruitInfo(
                        link = link, durationDays = DEFAULT_BATTLE_DURATION_DAYS, capacity = DEFAULT_BATTLE_CAPACITY, penalty = ""
                    )
                )
            }
            val post = postById(postId) ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
            return Result.success(post)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.updateCommunityRecruitPost(
                header, id, CommunityRecruitWriteRequest(title = title, content = content, battleUrl = link)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                mutate(postId) { post ->
                    post.copy(
                        title = title,
                        content = content,
                        subtitle = content,
                        battleInfo = post.battleInfo?.copy(link = link) ?: BattleRecruitInfo(
                            link = link, durationDays = DEFAULT_BATTLE_DURATION_DAYS, capacity = DEFAULT_BATTLE_CAPACITY, penalty = ""
                        )
                    )
                }
                Result.success(postById(postId)!!)
            } else {
                Result.failure(errorFrom(response, "햄배틀 모집 글 수정에 실패했습니다."))
            }
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            removeById(postId)
            return Result.success(Unit)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.deleteCommunityPost(header, id)
            if (response.isSuccessful) {
                removeById(postId)
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "게시글 삭제에 실패했습니다."))
            }
        }
    }


    override suspend fun toggleLike(postId: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                val liked = !post.isLiked
                post.copy(isLiked = liked, likeCount = (post.likeCount + if (liked) 1 else -1).coerceAtLeast(0))
            }
            return Result.success(Unit)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.toggleCommunityLike(header, id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                mutate(postId) { it.copy(isLiked = data.isLiked, likeCount = data.likeCount) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "좋아요 처리에 실패했습니다."))
            }
        }
    }

    override suspend fun toggleSave(postId: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { it.copy(isSaved = !it.isSaved) }
            return Result.success(Unit)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.toggleCommunityBookmark(header, id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                mutate(postId) { it.copy(isSaved = data.isBookmarked) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "북마크 처리에 실패했습니다."))
            }
        }
    }


    override suspend fun addComment(postId: String, content: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                val comment = TipComment(
                    id = newId("comment"), authorId = activeUserId, authorName = activeUserName,
                    content = content, timeLabel = JUST_NOW_LABEL
                )
                post.copy(comments = post.comments + comment, commentCount = post.commentCount + 1)
            }
            return Result.success(Unit)
        }
        val id = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.createCommunityComment(
                header, id, CommunityCommentWriteRequest(parentCommentId = null, content = content)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                mutate(postId) { post ->
                    val comment = TipComment(
                        id = data.commentId.toString(), authorId = activeUserId, authorName = activeUserName,
                        content = content, timeLabel = JUST_NOW_LABEL
                    )
                    post.copy(comments = post.comments + comment, commentCount = post.commentCount + 1)
                }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "댓글 작성에 실패했습니다."))
            }
        }
    }

    override suspend fun addReply(postId: String, commentId: String, content: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                val reply = TipReply(
                    id = newId("reply"), authorId = activeUserId, authorName = activeUserName,
                    content = content, timeLabel = JUST_NOW_LABEL
                )
                val comments = post.comments.map { comment ->
                    if (comment.id == commentId) comment.copy(replies = comment.replies + reply) else comment
                }
                post.copy(comments = comments, commentCount = post.commentCount + 1)
            }
            return Result.success(Unit)
        }
        val postIdLong = postId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_POST_NOT_FOUND", "게시글을 찾을 수 없습니다."))
        val parentId = commentId.toLongOrNull()
            ?: return Result.failure(ApiException("COMMUNITY_PARENT_COMMENT_NOT_FOUND", "부모 댓글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.createCommunityComment(
                header, postIdLong, CommunityCommentWriteRequest(parentCommentId = parentId, content = content)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                mutate(postId) { post ->
                    val reply = TipReply(
                        id = data.commentId.toString(), authorId = activeUserId, authorName = activeUserName,
                        content = content, timeLabel = JUST_NOW_LABEL
                    )
                    val comments = post.comments.map { comment ->
                        if (comment.id == commentId) comment.copy(replies = comment.replies + reply) else comment
                    }
                    post.copy(comments = comments, commentCount = post.commentCount + 1)
                }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "답글 작성에 실패했습니다."))
            }
        }
    }

    override suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                val comments = post.comments.map { comment ->
                    if (comment.id == commentId) comment.copy(isDeleted = true) else comment
                }
                post.copy(comments = comments, commentCount = (post.commentCount - 1).coerceAtLeast(0))
            }
            return Result.success(Unit)
        }
        val id = commentId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.deleteCommunityComment(header, id)
            if (response.isSuccessful) {
                mutate(postId) { post ->
                    val comments = post.comments.map { comment ->
                        if (comment.id == commentId) comment.copy(isDeleted = true) else comment
                    }
                    post.copy(comments = comments, commentCount = (post.commentCount - 1).coerceAtLeast(0))
                }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "댓글 삭제에 실패했습니다."))
            }
        }
    }

    override suspend fun deleteReply(postId: String, commentId: String, replyId: String): Result<Unit> {
        if (!CommunityConfig.USE_SERVER_COMMUNITY) {
            mutate(postId) { post ->
                val comments = post.comments.map { comment ->
                    if (comment.id != commentId) return@map comment
                    val replies = comment.replies.map { reply ->
                        if (reply.id == replyId) reply.copy(isDeleted = true) else reply
                    }
                    comment.copy(replies = replies)
                }
                post.copy(comments = comments, commentCount = (post.commentCount - 1).coerceAtLeast(0))
            }
            return Result.success(Unit)
        }
        val id = replyId.toLongOrNull() ?: return Result.failure(ApiException("COMMUNITY_COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = apiService.deleteCommunityComment(header, id)
            if (response.isSuccessful) {
                mutate(postId) { post ->
                    val comments = post.comments.map { comment ->
                        if (comment.id != commentId) return@map comment
                        val replies = comment.replies.map { reply ->
                            if (reply.id == replyId) reply.copy(isDeleted = true) else reply
                        }
                        comment.copy(replies = replies)
                    }
                    post.copy(comments = comments, commentCount = (post.commentCount - 1).coerceAtLeast(0))
                }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "답글 삭제에 실패했습니다."))
            }
        }
    }


    override fun canDeletePost(post: TipPost): Boolean =
        post.authorId == activeUserId || authRepository.isEditor

    override fun canDeleteComment(post: TipPost, comment: TipComment): Boolean =
        post.authorId == activeUserId || comment.authorId == activeUserId || authRepository.isEditor

    override fun canDeleteReply(post: TipPost, reply: TipReply): Boolean =
        post.authorId == activeUserId || reply.authorId == activeUserId || authRepository.isEditor

    override fun joinBattle(postId: String) {
        mutate(postId) { post ->
            val info = post.battleInfo ?: return@mutate post
            if (info.isFull) return@mutate post
            val updatedInfo = if (info.currentMemberCount != null) {
                info.copy(currentMemberCount = info.currentMemberCount + 1)
            } else {
                if (activeUserId in info.participantIds) return@mutate post
                info.copy(participantIds = info.participantIds + activeUserId)
            }
            post.copy(battleInfo = updatedInfo)
        }
    }

    override fun resetForAccount() {
        _posts.value = if (CommunityConfig.USE_SERVER_COMMUNITY) emptyList() else mockDataSource.allPosts()
        nextId = 1000
    }
}
