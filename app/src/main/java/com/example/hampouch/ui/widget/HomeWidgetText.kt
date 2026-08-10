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

/**
 * Pretendard 굵기. androidx.glance.text.FontFamily는 시스템 글꼴 이름만 지원해서
 * res/font의 커스텀 Pretendard를 못 그린다(자세한 이유는 widget_text_medium.xml 참고).
 * 그래서 굵기별로 미리 만들어둔 [layoutRes]의 순수 TextView를 [AndroidRemoteViews]로 심어서 그린다.
 */
enum class PretendardWeight(val layoutRes: Int) {
    Medium(R.layout.widget_text_medium),
    SemiBold(R.layout.widget_text_semibold),
    Bold(R.layout.widget_text_bold)
}

/** [PretendardText] 안에서 글자를 가로로 어디에 붙일지. */
enum class WidgetTextAlign(val gravity: Int) {
    Start(Gravity.START or Gravity.CENTER_VERTICAL),
    Center(Gravity.CENTER),
    End(Gravity.END or Gravity.CENTER_VERTICAL)
}

/**
 * Pretendard 글꼴이 적용된 위젯 텍스트. androidx.glance.text.Text 대신 이걸 쓴다.
 *
 * 반드시 [modifier]에 크기(width/height 또는 fillMaxWidth+height)를 명시해야 한다.
 * Glance는 [AndroidRemoteViews]로 심는 뷰의 실제 내용을 측정할 수 없어서, 크기를 안 주면
 * 칸이 찌그러들면서 한글 글자가 위아래로 잘려 보이는 문제가 생긴다.
 */
@Composable
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
        setTextViewTextSize(R.id.widget_text, android.util.TypedValue.COMPLEX_UNIT_SP, fontSize.value)
        setInt(R.id.widget_text, "setGravity", textAlign.gravity)
        if (maxLines != null) {
            setInt(R.id.widget_text, "setMaxLines", maxLines)
        }
    }
    AndroidRemoteViews(remoteViews = remoteViews, modifier = modifier)
}
