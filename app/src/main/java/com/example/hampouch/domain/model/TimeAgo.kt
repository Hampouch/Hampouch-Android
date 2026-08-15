package com.example.hampouch.domain.model

fun formatTimeAgoLabel(minutesAgo: Int): String =
    if (minutesAgo < 60) "${minutesAgo}분전" else "${minutesAgo / 60}시간전"
