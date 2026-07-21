package com.example.hampouch.data.model

import com.example.hampouch.R

enum class HamTipsCategoryTab(val labelResId: Int, val category: TipCategory?) {
    ALL(R.string.hamtips_tab_all, null),
    WHAT_TO_EAT(R.string.tip_category_what_to_eat, TipCategory.WHAT_TO_EAT),
    SHOPPING(R.string.tip_category_shopping, TipCategory.SHOPPING),
    COOKING(R.string.tip_category_cooking, TipCategory.COOKING),
    DISCOUNT(R.string.tip_category_discount, TipCategory.DISCOUNT),
    RECRUIT(R.string.tip_category_recruit, TipCategory.RECRUIT)
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
