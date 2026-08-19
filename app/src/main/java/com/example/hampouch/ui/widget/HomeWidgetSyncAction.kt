package com.example.hampouch.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors

class HomeWidgetSyncAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            HomeWidgetWorkerEntryPoint::class.java
        ).homeWidgetStatePublisher().requestImmediateSync()
    }
}
