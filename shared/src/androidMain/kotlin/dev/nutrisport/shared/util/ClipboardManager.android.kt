package dev.nutrisport.shared.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboard
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    val context = LocalContext.current
    val appContext = context.applicationContext
    return remember(appContext) {
        object : ClipboardManager {
            @SuppressLint("ServiceCast")
            override fun copy(text: String) {
                val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboard
                clipboard.setPrimaryClip(ClipData.newPlainText("copied", text))
            }
        }
    }
}