package dev.nutrisport.shared.util

import androidx.compose.runtime.Composable

interface ClipboardManager {
    fun copy(text: String)
}

@Composable
expect fun rememberClipboardManager(): ClipboardManager