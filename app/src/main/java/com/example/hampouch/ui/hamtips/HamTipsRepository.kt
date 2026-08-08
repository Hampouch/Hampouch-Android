package com.example.hampouch.ui.hamtips

import androidx.compose.runtime.mutableStateListOf
import com.example.hampouch.data.model.BattleRecruitInfo
import com.example.hampouch.data.model.MenuRatingInfo
import com.example.hampouch.data.model.TipCategory
import com.example.hampouch.data.model.TipComment
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.model.TipPostType
import com.example.hampouch.data.model.TipReply
import com.example.hampouch.ui.session.UserSession

object HamTipsRepository {

    const val CURRENT_USER_ID = "user_me"
    const val CURRENT_USER_NAME = "절약왕민준"
    private const val DEFAULT_BATTLE_DURATION_DAYS = 7
    private const val DEFAULT_BATTLE_CAPACITY = 5
    private const val JUST_NOW_LABEL = "방금"

    private val activeUserId: String get() = UserSession.currentUser.id
    private val activeUserName: String get() = UserSession.currentUser.name

    private val posts = mutableStateListOf<TipPost>().apply { addAll(HamTipsMockData.allPosts()) }
    private var nextId = 1000

    val allPosts: List<TipPost> get() = posts

    fun postById(id: String): TipPost? = posts.find { it.id == id }

    private fun newId(prefix: String): String = "${prefix}_${nextId++}"

    private fun mutate(postId: String, transform: (TipPost) -> TipPost) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) posts[index] = transform(posts[index])
    }

    fun createTipPost(category: TipCategory, title: String, content: String, imageUris: List<String>): TipPost {
        val post = TipPost(
            id = newId("tip"),
            type = TipPostType.TIP,
            category = category,
            title = title,
            subtitle = content,
            content = content,
            authorId = activeUserId,
            authorName = activeUserName,
            isEditorAuthor = UserSession.isEditor,
            hasImage = imageUris.isNotEmpty(),
            imageUris = imageUris
        )
        posts.add(0, post)
        return post
    }

    fun updateTipPost(postId: String, category: TipCategory, title: String, content: String, imageUris: List<String>) {
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
    }

    fun createMenuPost(
        title: String,
        menuName: String,
        place: String,
        price: Int,
        rating: MenuRatingInfo,
        comment: String,
        imageUris: List<String>
    ): TipPost {
        val post = TipPost(
            id = newId("menu"),
            type = TipPostType.MENU,
            category = TipCategory.WHAT_TO_EAT,
            title = title,
            subtitle = comment,
            content = comment,
            authorId = activeUserId,
            authorName = activeUserName,
            isEditorAuthor = UserSession.isEditor,
            hasImage = imageUris.isNotEmpty(),
            imageUris = imageUris,
            menuName = menuName,
            place = place,
            price = price,
            menuRating = rating
        )
        posts.add(0, post)
        return post
    }

    fun createBattlePost(title: String, content: String, link: String): TipPost {
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
        posts.add(0, post)
        return post
    }

    fun deletePost(postId: String) {
        posts.removeAll { it.id == postId }
    }

    fun incrementViewCount(postId: String) {
        mutate(postId) { it.copy(viewCount = it.viewCount + 1) }
    }

    fun toggleLike(postId: String) {
        mutate(postId) { post ->
            val liked = !post.isLiked
            post.copy(isLiked = liked, likeCount = (post.likeCount + if (liked) 1 else -1).coerceAtLeast(0))
        }
    }

    fun toggleSave(postId: String) {
        mutate(postId) { it.copy(isSaved = !it.isSaved) }
    }

    fun addComment(postId: String, content: String) {
        mutate(postId) { post ->
            val comment = TipComment(
                id = newId("comment"),
                authorId = activeUserId,
                authorName = activeUserName,
                content = content,
                timeLabel = JUST_NOW_LABEL
            )
            post.copy(comments = post.comments + comment, commentCount = post.commentCount + 1)
        }
    }

    fun addReply(postId: String, commentId: String, content: String) {
        mutate(postId) { post ->
            val reply = TipReply(
                id = newId("reply"),
                authorId = activeUserId,
                authorName = activeUserName,
                content = content,
                timeLabel = JUST_NOW_LABEL
            )
            val comments = post.comments.map { comment ->
                if (comment.id == commentId) comment.copy(replies = comment.replies + reply) else comment
            }
            post.copy(comments = comments, commentCount = post.commentCount + 1)
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        mutate(postId) { post ->
            val target = post.comments.find { it.id == commentId } ?: return@mutate post
            val removedCount = 1
            val comments = post.comments.map { comment ->
                if (comment.id == commentId) comment.copy(isDeleted = true) else comment
            }
            post.copy(comments = comments, commentCount = (post.commentCount - removedCount).coerceAtLeast(0))
        }
    }

    fun deleteReply(postId: String, commentId: String, replyId: String) {
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
    }

    fun canDeletePost(post: TipPost): Boolean =
        post.authorId == activeUserId || UserSession.isEditor

    fun canDeleteComment(post: TipPost, comment: TipComment): Boolean =
        post.authorId == activeUserId || comment.authorId == activeUserId || UserSession.isEditor

    fun canDeleteReply(post: TipPost, reply: TipReply): Boolean =
        post.authorId == activeUserId || reply.authorId == activeUserId || UserSession.isEditor

    fun joinBattle(postId: String) {
        mutate(postId) { post ->
            val info = post.battleInfo ?: return@mutate post
            if (info.isFull || activeUserId in info.participantIds) return@mutate post
            post.copy(battleInfo = info.copy(participantIds = info.participantIds + activeUserId))
        }
    }
}
