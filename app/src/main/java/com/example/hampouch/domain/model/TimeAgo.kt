package com.example.hampouch.domain.model

/** "3분전" / "2시간전" 형태의 상대 시각 문구. */
fun formatTimeAgoLabel(minutesAgo: Int): String =
    if (minutesAgo < 60) "${minutesAgo}분전" else "${minutesAgo / 60}시간전"
