package com.example.hampouch.data.model

import com.example.hampouch.R

enum class TipCategory(val labelResId: Int) {
    WHAT_TO_EAT(R.string.tip_category_what_to_eat),
    SHOPPING(R.string.tip_category_shopping),
    COOKING(R.string.tip_category_cooking),
    DISCOUNT(R.string.tip_category_discount),
    RECRUIT(R.string.tip_category_recruit),
    RECORD(R.string.tip_category_record),
    ETC(R.string.tip_category_etc)
}

enum class TipPostType {
    TIP, MENU, BATTLE
}

data class MenuRatingInfo(
    val taste: Int = 0,
    val costEffectiveness: Int = 0,
    val mood: Int = 0
) {
    val average: Float get() = (taste + costEffectiveness + mood) / 3f
}

data class BattleRecruitInfo(
    val link: String,
    val durationDays: Int,
    val capacity: Int,
    val penalty: String,
    val participantIds: List<String> = emptyList(),
    val currentMemberCount: Int? = null
) {
    val isFull: Boolean get() = (currentMemberCount ?: participantIds.size) >= capacity
}

data class TipReply(
    val id: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timeLabel: String,
    val isDeleted: Boolean = false
)

data class TipComment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timeLabel: String,
    val isDeleted: Boolean = false,
    val replies: List<TipReply> = emptyList()
)

data class TipPost(
    val id: String,
    val type: TipPostType = TipPostType.TIP,
    val category: TipCategory,
    val title: String,
    val subtitle: String,
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val isEditorAuthor: Boolean = false,
    val postedMinutesAgo: Int = 0,
    val viewCount: Int = 0,
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val hasImage: Boolean = false,
    val imageUris: List<String> = emptyList(),
    val imageKeys: List<String> = emptyList(),
    val menuName: String = "",
    val place: String = "",
    val price: Int = 0,
    val menuRating: MenuRatingInfo? = null,
    val battleInfo: BattleRecruitInfo? = null,
    val comments: List<TipComment> = emptyList(),
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

enum class HamTipsCategoryTab(val labelResId: Int, val category: TipCategory?) {
    ALL(R.string.hamtips_tab_all, null),
    WHAT_TO_EAT(R.string.tip_category_what_to_eat, TipCategory.WHAT_TO_EAT),
    SHOPPING(R.string.tip_category_shopping, TipCategory.SHOPPING),
    COOKING(R.string.tip_category_cooking, TipCategory.COOKING),
    DISCOUNT(R.string.tip_category_discount, TipCategory.DISCOUNT),
    RECRUIT(R.string.tip_category_recruit, TipCategory.RECRUIT),
    RECORD(R.string.tip_category_record, TipCategory.RECORD),
    ETC(R.string.tip_category_etc, TipCategory.ETC)
}

enum class HamTipsSortOrder(val labelResId: Int) {
    LATEST(R.string.hamtips_sort_latest),
    POPULAR(R.string.hamtips_sort_popular),
    VIEWS(R.string.hamtips_sort_views)
}

enum class HamTipsFabMenuOption(val labelResId: Int) {
    SHARE_TIP(R.string.hamtips_fab_share_tip),
    RECRUIT_BATTLE(R.string.hamtips_fab_recruit_battle),
    RECOMMEND_MENU(R.string.hamtips_fab_recommend_menu)
}

enum class TipShareCategory(val labelResId: Int, val category: TipCategory) {
    SHOPPING(R.string.tip_category_shopping, TipCategory.SHOPPING),
    COOKING(R.string.tip_category_cooking, TipCategory.COOKING),
    DISCOUNT(R.string.tip_category_discount, TipCategory.DISCOUNT),
    RECORD(R.string.tip_category_record, TipCategory.RECORD),
    ETC(R.string.tip_category_etc, TipCategory.ETC)
}

enum class MenuRatingType(val labelResId: Int) {
    TASTE(R.string.hamtips_rating_taste),
    COST_EFFECTIVENESS(R.string.hamtips_rating_cost_effectiveness),
    MOOD(R.string.hamtips_rating_mood)
}

enum class TipMoreMenuAction(val labelResId: Int) {
    DELETE(R.string.hamtips_more_menu_delete),
    EDIT(R.string.hamtips_more_menu_edit),
    CLOSE(R.string.hamtips_more_menu_close)
}

enum class CommentMoreMenuAction(val labelResId: Int) {
    DELETE(R.string.hamtips_more_menu_delete),
    CLOSE(R.string.hamtips_more_menu_close)
}
