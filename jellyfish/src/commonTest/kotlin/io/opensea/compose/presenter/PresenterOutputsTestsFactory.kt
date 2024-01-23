package io.opensea.compose.presenter

import app.cash.turbine.test
import io.kotest.core.spec.style.stringSpec
import io.kotest.matchers.shouldBe

internal fun <Presenter : ComposePresenter<Props, Event, Output, UiModel>> presenterOutputsTests(
    presenterProvider: () -> Presenter,
    emitOutput: Presenter.(Output) -> Unit,
) = stringSpec {
  lateinit var presenter: Presenter

  beforeTest { presenter = presenterProvider() }

  "outputs does not emit upon subscription" { presenter.outputs.test { expectNoEvents() } }

  "outputs emits when emitOutput is invoked" {
    presenter.outputs.test {
      repeat(times = 3) { index ->
        val output = Output("output ${index + 1}")
        presenter.emitOutput(output)
        awaitItem() shouldBe output
      }
    }
  }

  "outputs does not cache previously emitted outputs" {
    val output1 = Output("output 1")
    val output2 = Output("output 2")
    val output3 = Output("output 3")
    presenter.run {
      emitOutput(output1)
      emitOutput(output2)
    }

    presenter.outputs.test {
      expectNoEvents()
      presenter.emitOutput(output3)
      awaitItem() shouldBe output3
    }
  }

  "outputs does not apply distinct until changed behavior" {
    val output = Output(value = "output")

    presenter.outputs.test {
      repeat(times = 3) {
        presenter.emitOutput(output)
        awaitItem() shouldBe output
      }
    }
  }
}
