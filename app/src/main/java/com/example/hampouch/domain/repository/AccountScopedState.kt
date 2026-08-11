package com.example.hampouch.domain.repository

/**
 * 계정이 바뀌면 비워야 하는 상태.
 *
 * 구현체는 [com.example.hampouch.di.AccountScopedStateModule]에 등록되고,
 * [com.example.hampouch.data.repository.AccountDataCoordinator]가 등록된 전부를 비운다.
 */
interface AccountScopedState {
    fun resetForAccount()
}
