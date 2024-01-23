package io.opensea.compose.presenter

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Stable
public abstract class StatelessPresenter<
    Props : Any,
    in Event : Any,
    Output : Any,
    out UiModel,
> : ComposePresenter<Props, Event, Output, UiModel> {

  private val _outputs = MutableSharedFlow<Output>(extraBufferCapacity = 20)

  override val outputs: Flow<Output> = _outputs.asSharedFlow()

  protected fun emitOutput(output: Output) {
    check(_outputs.tryEmit(output)) { "Output buffer overflow" }
  }
}
