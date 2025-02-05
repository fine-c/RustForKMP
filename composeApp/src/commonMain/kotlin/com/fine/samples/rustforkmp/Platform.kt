package com.fine.samples.rustforkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform