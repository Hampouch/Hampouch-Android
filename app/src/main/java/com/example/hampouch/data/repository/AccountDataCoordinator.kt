package com.example.hampouch.data.repository

import android.content.Context
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.login.LoginMockData
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import com.example.hampouch.ui.mypage.AllSettingsStore
import com.example.hampouch.ui.mypage.MyPageProfileStore
import com.example.hampouch.ui.mypage.RecordAlarmStore
import com.example.hampouch.ui.notification.NotificationStore
import com.example.hampouch.ui.takeabreak.TakeABreakStore

object AccountDataCoordinator {

    private var syncedUserId: String? = null

    /**
     * [email]로 로그인한 계정을 [LoginMockData]에서 찾아 [com.example.hampouch.data.model.User.isExistingMember]를
     * 확인한다. 고정 데모 계정(민준/에디터)이나 이미 한 번 로그인한 적 있는 계정은 항상 true라서 원래
     * 목데이터를 그대로 보여주고, 방금 가입한 신규 계정(false)만 온보딩 결과(입력값 또는 건너뛰기)로 첫
     * 챌린지를 결정한 뒤 [LoginMockData.markAsExistingMember]로 true로 바꿔 다음부터는 기존 회원처럼 취급한다.
     */
    fun syncIfNeeded(context: Context, userId: String, email: String?) {
        if (userId == syncedUserId) return
        syncedUserId = userId
        ExpenseDetailStore.resetForAccount()
        MiniChallengeStore.resetForAccount()
        TakeABreakStore.resetForAccount()
        RecordAlarmStore.resetForAccount()
        NotificationStore.resetForAccount()
        MyPageProfileStore.resetForAccount()
        AllSettingsStore.resetForAccount()
        HamBattleMockData.resetForAccount()

        val account = email?.let { e -> LoginMockData.accounts.find { it.email == e } }
        if (account == null || account.isExistingMember) {
            // 기존/고정 계정(또는 목데이터에 없는 서버 모드 계정) → 항상 원래 목데이터.
            ChallengeRepository.resetForAccount()
            return
        }

        // 신규 계정의 첫 로그인: 온보딩을 다 채웠으면 그 값으로, 건너뛰었으면(예약된 값 없음) 빈 상태로.
        val request = OnboardingDataStore.takeReservedRequest(account.email)
        ChallengeRepository.resetEmpty()
        if (request != null) {
            ChallengeRepository.startNewChallenge(request)
        }
        LoginMockData.markAsExistingMember(account.email)
    }
}
