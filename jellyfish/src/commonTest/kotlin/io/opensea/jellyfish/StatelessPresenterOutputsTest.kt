package io.opensea.jellyfish

import androidx.compose.runtime.Composable
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.flow.Flow

internal class StatelessPresenterOutputsTest :
    StringSpec({
      include(
          presenterOutputsTests(
              presenterProvider = { Presenter() },
              emitOutput = { output -> emitOutputForTest(output = output) },
          ),
      )
    }) {

  private class Presenter : StatelessPresenter<Props, Event, Output, UiModel>() {

    fun emitOutputForTest(output: Output) {
      emitOutput(output)
    }

    @Composable
    override fun invoke(props: Props, events: Flow<Event>): UiModelWrapper<UiModel> {
      throw NotImplementedError("Not implemented")
    }
  }
}
