package com.example.simplelauncher.utils

import kotlinx.coroutines.CoroutineDispatcher

interface ICoroutineDispatchers {
    val io: CoroutineDispatcher
    val computation: CoroutineDispatcher
    val main: CoroutineDispatcher
}