package com.example.hampouch.ui.session

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.User
import com.example.hampouch.data.model.UserRole
import com.example.hampouch.ui.login.LoginMockData

object UserSession {
    private val currentUserState = mutableStateOf(LoginMockData.normalUser)
    private val isLoggedInState = mutableStateOf(false)

    val currentUser: User get() = currentUserState.value

    val isLoggedIn: Boolean get() = isLoggedInState.value

    val isEditor: Boolean get() = currentUserState.value.role == UserRole.EDITOR

    /**
     * 로그인 세션의 유일한 소스는 [com.example.hampouch.data.repository.AuthRepository]의
     * DataStore 기반 세션이다. 여기서는 그 값을 화면에서 동기적으로 읽기 쉬운 형태로 반영만 한다.
     * 앱을 재시작했을 때도 [com.example.hampouch.data.repository.AuthRepository.saveSession]을
     * 다시 호출해 이 함수로 동기화하며, 별도 저장소를 따로 두지 않는다(예전엔 SharedPreferences에
     * 로그인 유저 id를 저장해 뒀다가 목데이터 계정 목록에서 재조회했는데, 실제 서버 로그인 유저는
     * 그 목록에 없어서 앱을 재실행하면 항상 기본 목데이터 계정으로 되돌아가는 버그가 있었다).
     */
    fun login(context: Context, user: User) {
        currentUserState.value = user
        isLoggedInState.value = true
    }

    fun logout(context: Context) {
        isLoggedInState.value = false
        currentUserState.value = LoginMockData.normalUser
    }

    // TODO: 서버팀 회원 탈퇴 API 연동 시 실제 계정 삭제 요청으로 교체. 현재는 목데이터 계정 목록에서
    // 제거해 같은 이메일/비밀번호로 다시 로그인할 수 없게 만드는 것으로 탈퇴를 흉내낸다.
    fun withdraw(context: Context) {
        LoginMockData.removeAccount(currentUserState.value.id)
        logout(context)
    }
}
