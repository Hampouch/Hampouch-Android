package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.HamTipsSortOrder
import com.example.hampouch.domain.model.MenuRatingInfo
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.TipComment
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipReply
import kotlinx.coroutines.flow.StateFlow

interface HamTipsRepository {

    val posts: StateFlow<List<TipPost>>

    fun postById(id: String): TipPost?

    suspend fun loadHome(sortType: HamTipsSortOrder = HamTipsSortOrder.LATEST): Result<Unit>

    suspend fun loadCategoryPosts(
        category: TipCategory,
        sortType: HamTipsSortOrder = HamTipsSortOrder.LATEST
    ): Result<Unit>

    suspend fun loadPopularPosts(sortType: HamTipsSortOrder = HamTipsSortOrder.LATEST): Result<Unit>

    suspend fun loadPochipickPosts(sortType: HamTipsSortOrder = HamTipsSortOrder.LATEST): Result<Unit>

    suspend fun loadMyPosts(sortType: HamTipsSortOrder = HamTipsSortOrder.LATEST): Result<Unit>

    suspend fun loadSavedPosts(sortType: HamTipsSortOrder = HamTipsSortOrder.LATEST): Result<Unit>

    suspend fun loadPostDetail(postId: String): Result<TipPost>

    suspend fun createTipPost(
        category: TipCategory,
        title: String,
        content: String,
        imageUris: List<String>,
        imageKeys: List<String> = emptyList()
    ): Result<TipPost>

    suspend fun updateTipPost(
        postId: String,
        category: TipCategory,
        title: String,
        content: String,
        imageUris: List<String>,
        imageKeys: List<String> = emptyList()
    ): Result<TipPost>

    suspend fun createMenuPost(
        title: String,
        menuName: String,
        place: String,
        price: Int,
        rating: MenuRatingInfo,
        comment: String,
        imageUris: List<String>,
        imageKeys: List<String> = emptyList()
    ): Result<TipPost>

    suspend fun updateMenuPost(
        postId: String,
        title: String,
        menuName: String,
        place: String,
        price: Int,
        rating: MenuRatingInfo,
        comment: String,
        imageUris: List<String>,
        imageKeys: List<String> = emptyList()
    ): Result<TipPost>

    suspend fun createBattlePost(title: String, content: String, link: String): Result<TipPost>

    suspend fun updateBattlePost(
        postId: String,
        title: String,
        content: String,
        link: String
    ): Result<TipPost>

    suspend fun deletePost(postId: String): Result<Unit>

    suspend fun toggleLike(postId: String): Result<Unit>

    suspend fun toggleSave(postId: String): Result<Unit>

    suspend fun addComment(postId: String, content: String): Result<Unit>

    suspend fun addReply(postId: String, commentId: String, content: String): Result<Unit>

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit>

    suspend fun deleteReply(postId: String, commentId: String, replyId: String): Result<Unit>

    fun canDeletePost(post: TipPost): Boolean

    fun canDeleteComment(post: TipPost, comment: TipComment): Boolean

    fun canDeleteReply(post: TipPost, reply: TipReply): Boolean

    fun joinBattle(postId: String)
}
