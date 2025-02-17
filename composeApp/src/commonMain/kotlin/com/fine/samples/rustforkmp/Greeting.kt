package com.fine.samples.rustforkmp

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "${platform.name}!"
    }
}