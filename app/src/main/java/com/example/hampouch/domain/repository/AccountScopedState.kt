package com.example.hampouch.domain.repository

/**
 * 계정이 바뀌면 비워야 하는 상태.
 *
 * 예전에는 `AccountDataCoordinator`가 화면 패키지의 스토어들을 하나씩 직접 호출해서
 * data 레이어가 ui를 거꾸로 참조하고 있었다. 이 인터페이스를 구현해 두면
 * [com.example.hampouch.di.AccountScopedStateModule]에서 등록되고, 코디네이터는
 * 무엇이 등록됐는지 모른 채 전부 비운다.
 */
interface AccountScopedState {
    fun resetForAccount()
}
