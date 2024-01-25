# Jellyfish
***Build presenters as declarative state machines with Compose***

Heavily inspired by [Square's Workflow library](https://square.github.io/workflow/), Jellyfish has some core properties:
- Presenters are declaratively defined state-machines
- Separation of internal presenter state and UI models
- Allows starting of a presenter in any given state, which makes testing nice and easy
- Supports nested composition of presenters

 Jellyfish is production ready and has been used in the OpenSea Android and iOS apps (via Kotlin Multiplatform) for over a year.

## Setup
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = URI("https://jitpack.io") }
    }
}

dependencies {
  implementation("com.github.ProjectOpenSea:jellyfish:0.1.0")
}
```
Since artifact distribution is through Jitpack for now, **Kotlin Multiplatform iOS artifacts are not built.** To use Jellyfish in a iOS KMP project, you'll need to **build the artifacts yourself.** Migrating to Maven is on the roadmap.

## Example usage
As a (sort of contrived) example, let's build a simple counter that fetches it's initial count from another source. First, let's define our states, their corresponding UI models, and events.
```kotlin
sealed class CounterUiModel
data object LoadingUi : CounterUiModel()
data class LoadedUi(val text: String) : CounterUiModel()
data class ErrorUi(val message: String) : CounterUiModel()

sealed class CounterState
data object Loading : CounterState()
data class Loaded(val count: Int) : CounterState()
data object Error : CounterState()

sealed class CounterEvent
sealed class CounterLoadedEvent : CounterEvent()
data object Increment : CounterLoadedEvent()
sealed class CounterErrorEvent : CounterEvent()
data object Retry : CounterErrorEvent()
```

And this is what our presenter would look like:
```kotlin
// note: this class doesn't use Props or Output, so we can set their types to Unit.
class CounterPresenter : StateMachinePresenter<CounterState, Unit, CounterEvent, Unit, CounterUiModel>() {

    // define the state we should initialize into
    override fun initialState(props: Unit): CounterState = Loading

    @Composable
    override fun render(state: CounterState, props: Unit, events: Flow<CounterEvent>): CounterUiModel {
        // map our state to UI models
        return when (state) {
            Loading -> LoadingUi
            is Loaded -> LoadedUi(text = "Count: ${state.count}")
            Error -> ErrorUi("Couldn't load initial count.")
        }
    }

    @Composable
    override fun state(
        state: CounterState,
        setState: (CounterState) -> Unit,
        props: Unit,
        events: Flow<CounterEvent>
    ): CounterState {
        when (state) {
            Loading -> {
                LaunchedEffect(Unit) {
                    val initialCountResult = fetchCount() // example suspending call that returns a Result

                    // we use the setState() function to update our state.
                    initialCountResult.getOrNull()?.let { initialCount ->
                        setState(Loaded(count = initialCount))
                    } ?: setState(Error)
                }
            }
            is Loaded -> {
                // EventStateTransitions is a helper function that helps enforce we are only accepting the events we declare in this state.
                EventStateTransitions<_, CounterLoadedEvent>(
                    state = state,
                    setState = setState,
                    props = props,
                    events = events,
                ) { event, _ ->
                    // this lambda defines what state we should transition to when receiving an event.
                    when (event) {
                        Increment -> Loaded(count = count+1)
                    }
                }
            }
            Error -> {
                EventStateTransitions<_, CounterErrorEvent>(
                    state = state,
                    setState = setState,
                    props = props,
                    events = events,
                ) { event, _ ->
                    when (event) {
                        Retry -> Loading
                    }
                }
            }
        }

        return state
    }
}

```

## Nested presenters
🚧🚧🚧🚧🚧🚧 under construction 🚧🚧🚧🚧🚧🚧

## License
Apache License Copyright 2023 Ozone Networks Inc.

Portions of this software include code from [Jetbrains Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)  which is licensed under the Apache License Version 2.0 and which is copyright 2020-2021 JetBrains s.r.o. and their respective authors and developers 
