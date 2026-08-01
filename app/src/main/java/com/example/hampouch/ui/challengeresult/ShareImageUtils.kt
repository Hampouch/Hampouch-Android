package com.example.hampouch.ui.challengeresult

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Telephony

fun defaultSmsPackage(context: Context): String? = Telephony.Sms.getDefaultSmsPackage(context)

// 공유용 이미지는 앱 캐시(FileProvider)가 아니라 MediaStore에 저장한다.
fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "hampouch_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Hampouch")
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
    val success = runCatching {
        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }.isSuccess
    return if (success) uri else null
}

fun buildShareImageIntent(imageUri: Uri, packageName: String?): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (packageName != null) setPackage(packageName)
    }

// 대상 앱이 내부적으로 다른 컴포넌트로 넘겨서 첨부를 처리하는 경우에도
// 읽기 권한이 유지되도록 패키지 단위로 명시적으로 권한을 부여한다.
fun grantShareUriPermission(context: Context, packageName: String, imageUri: Uri) {
    context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

// 공유가 끝난 뒤(공유 대상 앱에서 돌아온 시점) 임시로 저장했던 공유용 이미지를 정리한다.
// "이미지로 저장"을 직접 선택한 경우에는 호출하지 않아 갤러리에 그대로 남는다.
fun deleteImage(context: Context, uri: Uri): Boolean =
    runCatching { context.contentResolver.delete(uri, null, null) > 0 }.getOrDefault(false)
