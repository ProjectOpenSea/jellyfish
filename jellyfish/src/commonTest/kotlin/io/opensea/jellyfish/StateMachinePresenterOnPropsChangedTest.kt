package io.opensea.jellyfish

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow

internal class StateMachinePresenterOnPropsChangedTest : StringSpec() {

  init {
    "when prop update is handled by onPropsChanged, don't reset the presenter's state" {
      val presenter = PresenterWithOverride()

      var props by mutableStateOf(Props(name = "Tester 1", description = "Desc 1"))
      val events = MutableSharedFlow<Event>()

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = events) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 0")

            events.emit(Event(value = 1))
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 1")

            props = Props(name = "Tester 2", description = "Desc 1")
            // props update triggers recomposition before onPropsChanged is called, resulting in
            // previous value re-emission
            skipItems(1)
            awaitItem().value shouldBe UiModel("Tester 2 - Desc 1 - 1")
          }
    }

    "when prop update isn't handled by onPropsChanged, do reset the presenter's state" {
      val presenter = PresenterWithOverride()

      var props by mutableStateOf(Props(name = "Tester 1", description = "Desc 1"))
      val events = MutableSharedFlow<Event>()

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = events) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 0")

            events.emit(Event(value = 1))
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 1")

            props = Props(name = "Tester 1", description = "Desc 2")
            // props update triggers recomposition before onPropsChanged is called, resulting in
            // previous value re-emission
            skipItems(1)
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 2 - 0")
          }
    }

    "when onPropsChanged does not handle specific prop changes, do reset the presenter's state" {
      val presenter = PresenterWithoutOverride()

      var props by mutableStateOf(Props(name = "Tester 1", description = "Desc 1"))
      val events = MutableSharedFlow<Event>()

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = events) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 0")

            events.emit(Event(value = 1))
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 1")

            props = Props(name = "Tester 2", description = "Desc 1")
            // props update triggers recomposition before onPropsChanged is called, resulting in
            // previous value re-emission
            skipItems(1)
            awaitItem().value shouldBe UiModel("Tester 2 - Desc 1 - 0")
          }
    }

    "whenever invoke is called with equivalent props, ensure we don't re-render" {
      val presenter = PresenterWithOverride()

      var props by mutableStateOf(Props(name = "Tester 1", description = "Desc 1"))

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = emptyFlow()) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - Desc 1 - 0")

            props = Props(name = "Tester 1", description = "Desc 1")

            expectNoEvents()
          }
    }
  }

  private open class PresenterWithoutOverride :
      StateMachinePresenter<State, Props, Event, Output, UiModel>() {
    @Composable
    override fun render(state: State, props: Props, events: Flow<Event>) =
        UiModel(text = "${state.name} - ${state.description} - ${state.count}")

    override fun initialState(props: Props) =
        State(
            name = props.name,
            description = props.description,
            count = 0,
        )

    @Composable
    override fun state(
        state: State,
        setState: (State) -> Unit,
        props: Props,
        events: Flow<Event>,
    ): State {
      EventStateTransitions<_, Event>(state, setState, props, events) { event, _ ->
        state.copy(count = state.count + event.value)
      }

      return state
    }
  }

  private class PresenterWithOverride : PresenterWithoutOverride() {
    override fun onPropsChanged(old: Props, new: Props, state: State): State {
      return if (old.name != new.name) {
        state.copy(name = new.name)
      } else {
        initialState(new)
      }
    }
  }
}
