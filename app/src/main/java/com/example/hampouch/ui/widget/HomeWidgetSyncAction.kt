package com.example.hampouch.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors

/** 위젯의 수동 동기화 버튼이 기존 서버 동기화 Worker를 즉시 실행하도록 연결한다. */
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
