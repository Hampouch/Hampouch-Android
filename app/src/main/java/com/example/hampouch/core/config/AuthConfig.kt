package com.example.hampouch.core.config

/**
 * 로그인/회원가입을 실제 서버로 붙일지, 서버 없이 목데이터([com.example.hampouch.ui.login.LoginMockData])로
 * 돌릴지 전환하는 스위치.
 *
 * - true  : [com.example.hampouch.core.network.NetworkModule]을 통해 실제 서버(/api/auth 이하 엔드포인트)로 요청한다.
 * - false : 서버 호출 없이 [com.example.hampouch.ui.login.LoginMockData] 기반으로 로그인/회원가입을 흉내낸다.
 *
 * 서버 없이 화면/네비게이션만 확인하고 싶을 때는 false로, 실제 서버 연동을 테스트할 때는 true로 두고 빌드한다.
 * 값은 로컬에서만 바꿔서 테스트하고, 커밋 시에는 팀에서 합의한 기본값을 유지한다.
 */
object AuthConfig {
    const val USE_SERVER_AUTH: Boolean = true
}
