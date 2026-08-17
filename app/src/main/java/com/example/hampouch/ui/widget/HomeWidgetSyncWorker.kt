package com.example.hampouch.ui.widget

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "home_widget_server_sync"
private const val SYNC_INTERVAL_MINUTES = 15L

object HomeWidgetSyncScheduler {
    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<HomeWidgetSyncWorker>(
            SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

class HomeWidgetSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(
            applicationContext,
            HomeWidgetWorkerEntryPoint::class.java
        )
        val authRepository = dependencies.authRepository()
        if (authRepository.userSession.first() == null) {
            dependencies.homeWidgetStatePublisher().publishLoggedOut()
            return Result.success()
        }

        return try {
            val today = LocalDate.now()
            val results = listOf(
                dependencies.restRepository().syncStatus(),
                dependencies.challengeRepository().loadCurrentChallenge(),
                dependencies.expenseRepository().loadDay(today)
            )
            if (authRepository.userSession.first() == null) {
                dependencies.homeWidgetStatePublisher().publishLoggedOut()
                Result.success()
            } else if (results.all { it.isSuccess }) {
                dependencies.homeWidgetStatePublisher().publishAfterHomeSync()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HomeWidgetWorkerEntryPoint {
    fun authRepository(): AuthRepository

    fun challengeRepository(): ChallengeRepository

    fun expenseRepository(): ExpenseRepository

    fun restRepository(): RestRepository

    fun homeWidgetStatePublisher(): HomeWidgetStatePublisher
}
