package io.opensea.compose.presenter

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

public open class TestComposePresenter<Props : Any, Event : Any, Output : Any, UiModel>(
    private val stubUiModelKey: String,
) : ComposePresenter<Props, Event, Output, UiModel> {
  override val outputs: Flow<Output> = emptyFlow()

  @Composable
  override fun invoke(props: Props, events: Flow<Event>): UiModelWrapper<UiModel> =
      StubUiModelWrapper(stubUiModelKey)
}
