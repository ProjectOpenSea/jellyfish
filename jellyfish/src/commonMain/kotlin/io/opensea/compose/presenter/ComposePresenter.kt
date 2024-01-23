@file:OptIn(ExperimentalObjCRefinement::class)

package io.opensea.compose.presenter

import androidx.compose.runtime.Composable
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlinx.coroutines.flow.Flow

public interface ComposePresenter<in Props : Any, in Event : Any, out Output : Any, out UiModel> {
  public val outputs: Flow<Output>

  @HiddenFromObjC
  @Composable
  public operator fun invoke(props: Props, events: Flow<Event>): UiModelWrapper<UiModel>
}
