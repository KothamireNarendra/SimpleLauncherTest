package com.example.simplelauncher.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class AppCoroutineDispatchers(
    override val io: CoroutineDispatcher = Dispatchers.IO,
    override val computation: CoroutineDispatcher = Dispatchers.Default,
    override val main: CoroutineDispatcher = Dispatchers.Main
) : ICoroutineDispatchers