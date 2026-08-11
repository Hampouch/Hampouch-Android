package com.example.hampouch.data.local

import com.example.hampouch.domain.model.MiniChallengeDaySummary
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import java.time.LocalDate

/**
 * 미니 챌린지의 로컬 상태 저장소.
 *
 * 서버 응답을 반영하는 쪽은 [com.example.hampouch.data.repository.MiniChallengeRepository]이고,
 * 실제 상태는 아직 화면 패키지의 `MiniChallengeStore`가 들고 있다. data가 ui를 거꾸로 참조하지
 * 않도록 이 인터페이스로 끊는다.
 *
 * TODO: 상태를 data 레이어의 StateFlow로 옮기고 화면은 ViewModel로 구독하게 바꾸면 이 인터페이스는
 *  Repository 본체에 흡수된다. 그때 화면 4곳(Home·MiniChallenge·Create·RecommendedList)의
 *  동기 읽기를 collectAsStateWithLifecycle로 바꿔야 한다.
 */
interface MiniChallengeLocalStore {
    fun setChallengesForDate(date: LocalDate, entries: List<MiniChallengeEntry>)
    fun setSummaryForDate(date: LocalDate, summary: MiniChallengeDaySummary)
    fun replaceRecommendedChallenges(items: List<RecommendedMiniChallenge>)
    fun removeRecommended(id: String)
    fun addRecommendedChallenge(date: LocalDate, recommended: RecommendedMiniChallenge): Boolean
    fun addChallenge(date: LocalDate, name: String, totalDays: Int?): Boolean
    fun removeChallenge(date: LocalDate, id: String)
    fun toggle(date: LocalDate, id: String)
}
