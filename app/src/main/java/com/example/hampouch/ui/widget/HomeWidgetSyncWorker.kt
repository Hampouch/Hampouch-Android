package com.example.hampouch.ui.widget

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "home_widget_server_sync"
private const val UNIQUE_IMMEDIATE_WORK_NAME = "home_widget_immediate_sync"
private const val SYNC_INTERVAL_MINUTES = 15L

object HomeWidgetSyncScheduler {
    private fun connectedNetworkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<HomeWidgetSyncWorker>(
            SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(connectedNetworkConstraints())
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

    fun enqueueNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<HomeWidgetSyncWorker>()
            .setConstraints(connectedNetworkConstraints())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}

class HomeWidgetSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val publisher = EntryPointAccessors.fromApplication(
            applicationContext,
            HomeWidgetWorkerEntryPoint::class.java
        ).homeWidgetStatePublisher()
        return if (publisher.syncFromServer()) Result.success() else Result.retry()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HomeWidgetWorkerEntryPoint {
    fun homeWidgetStatePublisher(): HomeWidgetStatePublisher
}
