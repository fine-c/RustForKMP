package com.fine.samples.rustforkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RustForKMP",
    ) {
        App()
    }
}