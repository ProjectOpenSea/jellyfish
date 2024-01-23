package io.opensea.jellyfish

import androidx.compose.runtime.Composable
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.flow.Flow

internal class StateMachinePresenterOutputsTest :
    StringSpec({
      include(
          presenterOutputsTests(
              presenterProvider = { Presenter() },
              emitOutput = { output -> emitOutputForTest(output = output) },
          ),
      )
    }) {

  private class Presenter : StateMachinePresenter<State, Props, Event, Output, UiModel>() {

    fun emitOutputForTest(output: Output) {
      emitOutput(output)
    }

    override fun initialState(props: Props): State {
      notImplemented()
    }

    @Composable
    override fun render(
        state: State,
        props: Props,
        events: Flow<Event>,
    ): UiModel {
      notImplemented()
    }

    @Composable
    override fun state(
        state: State,
        setState: (State) -> Unit,
        props: Props,
        events: Flow<Event>,
    ): State {
      notImplemented()
    }

    private fun notImplemented(): Nothing {
      throw NotImplementedError("Not implemented")
    }
  }
}
