package com.hypersoft.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @Author: SOHAIB AHMED
 * @Date: 14/03/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */


fun main() {
    runBlocking {
        for (i in 0..50) {
            val data = fetchData(i)
            println(data)
        }
    }
}

suspend fun fetchData(index: Int): String {
    println("Processing : $index")
    return suspendCancellableCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            if (continuation.isActive) {
                continuation.resume("Result # $index")
            }
        }
    }
}