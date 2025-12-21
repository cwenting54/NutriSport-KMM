package dev.nutrisport.app

import androidx.compose.ui.window.ComposeUIViewController
import dev.nutrisport.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }