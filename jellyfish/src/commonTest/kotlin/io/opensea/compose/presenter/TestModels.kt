package io.opensea.compose.presenter

import androidx.compose.runtime.Immutable

@Immutable internal data class UiModel(val text: String)

@Immutable
internal data class State(
    val name: String,
    val count: Int,
    val description: String = "description",
)

@Immutable
internal data class Props(
    val name: String,
    val description: String = "description",
)

@Immutable internal data class Event(val value: Int)

@Immutable internal data class Output(val value: String)
