package com.example.hampouch.ui.widget

import android.view.Gravity
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import com.example.hampouch.R

enum class PretendardWeight(val layoutRes: Int, val usesAutoSize: Boolean = false) {
    Regular(R.layout.widget_text_regular),
    Medium(R.layout.widget_text_medium),
    SemiBold(R.layout.widget_text_semibold),
    Bold(R.layout.widget_text_bold),
    Medium18AutoSize(R.layout.widget_text_medium_18_autosize, usesAutoSize = true),
    SemiBold16AutoSize(R.layout.widget_text_semibold_16_autosize, usesAutoSize = true),
    SemiBold18AutoSize(R.layout.widget_text_semibold_18_autosize, usesAutoSize = true),
    SemiBold24AutoSize(R.layout.widget_text_semibold_24_autosize, usesAutoSize = true),
    BoldAutoSize(R.layout.widget_text_bold_autosize, usesAutoSize = true)
}

enum class WidgetTextAlign(val gravity: Int) {
    Start(Gravity.START or Gravity.CENTER_VERTICAL),
    Center(Gravity.CENTER),
    End(Gravity.END or Gravity.CENTER_VERTICAL)
}

@Composable
@Suppress("LongParameterList")
fun PretendardText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    weight: PretendardWeight = PretendardWeight.Medium,
    textAlign: WidgetTextAlign = WidgetTextAlign.Start,
    maxLines: Int? = null,
    modifier: GlanceModifier
) {
    val context = LocalContext.current
    val remoteViews = RemoteViews(context.packageName, weight.layoutRes).apply {
        setTextViewText(R.id.widget_text, text)
        setTextColor(R.id.widget_text, color.toArgb())
        if (!weight.usesAutoSize) {
            setTextViewTextSize(R.id.widget_text, android.util.TypedValue.COMPLEX_UNIT_SP, fontSize.value)
        }
        setInt(R.id.widget_text, "setGravity", textAlign.gravity)
        if (maxLines != null) {
            setInt(R.id.widget_text, "setMaxLines", maxLines)
        }
    }
    AndroidRemoteViews(remoteViews = remoteViews, modifier = modifier)
}

internal fun formatWon(amount: Int): String = "%,d".format(amount)
