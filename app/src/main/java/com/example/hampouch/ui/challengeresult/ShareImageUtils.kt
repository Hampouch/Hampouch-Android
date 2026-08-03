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

fun grantShareUriPermission(context: Context, packageName: String, imageUri: Uri) {
    context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun deleteImage(context: Context, uri: Uri): Boolean =
    runCatching { context.contentResolver.delete(uri, null, null) > 0 }.getOrDefault(false)
