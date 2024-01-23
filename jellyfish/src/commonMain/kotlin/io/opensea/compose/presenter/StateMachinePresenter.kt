@file:OptIn(ExperimentalObjCRefinement::class)

package io.opensea.compose.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.reduce

@Stable
public abstract class StateMachinePresenter<
    State : Any,
    Props : Any,
    in Event : Any,
    Output : Any,
    out UiModel,
> : ComposePresenter<Props, Event, Output, UiModel> {

  private val _outputs = MutableSharedFlow<Output>(extraBufferCapacity = 20)

  override val outputs: Flow<Output> = _outputs.asSharedFlow()

  protected abstract fun initialState(props: Props): State

  protected open fun onPropsChanged(
      old: Props,
      new: Props,
      state: State,
  ): State {
    return initialState(new)
  }

  @HiddenFromObjC
  @Composable
  protected abstract fun render(state: State, props: Props, events: Flow<Event>): UiModel

  @HiddenFromObjC
  @Composable
  protected abstract fun state(
      state: State,
      setState: (State) -> Unit,
      props: Props,
      events: Flow<Event>,
  ): State

  @HiddenFromObjC
  @Composable
  protected inline fun <SubState : State, reified SubEvent : Event> EventStateTransitions(
      state: SubState,
      noinline setState: (State) -> Unit,
      props: Props,
      events: Flow<Event>,
      crossinline transition: SubState.(event: SubEvent, props: Props) -> State,
  ) {
    val updatedState by rememberUpdatedState(state)
    val updatedProps by rememberUpdatedState(props)
    val updatedSetState by rememberUpdatedState(setState)

    LaunchedEffect(events) {
      events.filterIsInstance<SubEvent>().collect { event ->
        updatedSetState(updatedState.transition(event, updatedProps))
      }
    }
  }

  protected fun emitOutput(output: Output) {
    check(_outputs.tryEmit(output)) { "Output buffer overflow" }
  }

  @Composable
  public override operator fun invoke(props: Props, events: Flow<Event>): UiModelWrapper<UiModel> {
    return UiModelWrapper(internalRender(internalState(null, props, events), props, events))
  }

  @Composable
  internal fun internalRender(state: State, props: Props, events: Flow<Event>): UiModel {
    return render(state, props, events)
  }

  @Composable
  internal fun internalState(initialState: State?, props: Props, events: Flow<Event>): State {
    val (state, setState) = remember { mutableStateOf(initialState ?: initialState(props)) }

    val updatedState by rememberUpdatedState(state)
    val updatedProps by rememberUpdatedState(props)
    val updatedSetState by rememberUpdatedState(setState)

    LaunchedEffect(Unit) {
      snapshotFlow { updatedProps }
          .reduce { oldProps, newProps ->
            updatedSetState(onPropsChanged(oldProps, newProps, updatedState))
            newProps
          }
    }

    return state(state = state, setState = setState, props = props, events = events)
  }
}

@Composable
public fun <State : Any, Props : Any, Event : Any, Output : Any, UiModel> StateMachinePresenter<
    State, Props, Event, Output, UiModel>
    .testFromState(initialState: State?, props: Props, events: Flow<Event>): State {
  return internalState(initialState, props, events)
}

@Composable
public fun <State : Any, Props : Any, Event : Any, Output : Any, UiModel> StateMachinePresenter<
    State, Props, Event, Output, UiModel>
    .testRender(initialState: State, props: Props, events: Flow<Event>): UiModel {
  return internalRender(initialState, props, events)
}
