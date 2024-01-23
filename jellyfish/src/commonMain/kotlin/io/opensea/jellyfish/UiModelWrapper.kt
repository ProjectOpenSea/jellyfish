package io.opensea.jellyfish

import kotlin.reflect.KProperty

// Wrapper interface for UiModel types so that we can stub out
// child UiModels in tests.
public interface UiModelWrapper<out T> {
  public val value: T

  public operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value

  public companion object {
    public operator fun <T> invoke(value: T): UiModelWrapper<T> {
      return UiModelWrapperImpl(value = value)
    }
  }
}

private data class UiModelWrapperImpl<out T>(override val value: T) : UiModelWrapper<T>

public data class StubUiModelWrapper<T>(val key: String) : UiModelWrapper<T> {
  override val value: T
    get() = error("This is a stub.")
}
