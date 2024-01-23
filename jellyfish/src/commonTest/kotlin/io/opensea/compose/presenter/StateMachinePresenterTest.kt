package io.opensea.compose.presenter

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

internal class StateMachinePresenterTest : StringSpec() {

  init {
    "invoke render and state integration test" {
      val presenter =
          object : StateMachinePresenter<State, Unit, Unit, Nothing, UiModel>() {
            @Composable
            override fun render(state: State, props: Unit, events: Flow<Unit>) =
                UiModel(text = "${state.name} - ${state.count + 5}")

            override fun initialState(props: Unit) = State(name = "Tester", count = 1)

            @Composable
            override fun state(
                state: State,
                setState: (State) -> Unit,
                props: Unit,
                events: Flow<Unit>,
            ) = state
          }

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = Unit, events = emptyFlow()) }
          .test { awaitItem().value shouldBe UiModel("Tester - 6") }
    }

    "whenever invoke is called with separate props, ensure we render using the new props" {
      val presenter = PresenterWithProps()

      var props by mutableStateOf(Props(name = "Tester 1"))

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = emptyFlow()) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - 0")

            props = Props(name = "Tester 2")
            // props update triggers recomposition before onPropsChanged is called, resulting in
            // previous value re-emission
            skipItems(1)

            awaitItem().value shouldBe UiModel("Tester 2 - 0")
          }
    }

    "whenever invoke is called with equivalent props, ensure we don't re-render" {
      val presenter = PresenterWithProps()

      var props by mutableStateOf(Props(name = "Tester 1"))

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = emptyFlow()) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - 0")

            props = Props(name = "Tester 1")

            expectNoEvents()
          }
    }

    "render and state + EventStateTransitions integration test" {
      val presenter = PresenterWithProps()

      var props by mutableStateOf(Props(name = "Tester 1"))
      val events = MutableSharedFlow<Event>()

      moleculeFlow(RecompositionMode.Immediate) { presenter(props = props, events = events) }
          .test {
            awaitItem().value shouldBe UiModel("Tester 1 - 0")

            events.emit(Event(value = 1))
            awaitItem().value shouldBe UiModel("Tester 1 - 1")

            events.emit(Event(value = 2))
            awaitItem().value shouldBe UiModel("Tester 1 - 3")

            props = Props(name = "Tester 2")
            // props update triggers recomposition before onPropsChanged is called, resulting in
            // previous value re-emission
            skipItems(1)
            awaitItem().value shouldBe UiModel("Tester 2 - 0")

            events.emit(Event(value = 5))
            awaitItem().value shouldBe UiModel("Tester 2 - 5")

            expectNoEvents()
          }
    }
  }

  private class PresenterWithProps : StateMachinePresenter<State, Props, Event, Output, UiModel>() {
    @Composable
    override fun render(state: State, props: Props, events: Flow<Event>) =
        UiModel(text = "${state.name} - ${state.count}")

    override fun initialState(props: Props) = State(name = props.name, count = 0)

    @Composable
    override fun state(
        state: State,
        setState: (State) -> Unit,
        props: Props,
        events: Flow<Event>,
    ): State {
      EventStateTransitions<_, Event>(state, setState, props, events) { event, currentProps ->
        State(name = currentProps.name, count = count + event.value)
      }
      return state
    }
  }
}
