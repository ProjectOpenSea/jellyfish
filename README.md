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


## Usage and Examples
🚧🚧🚧🚧🚧🚧 under construction 🚧🚧🚧🚧🚧🚧

## License
Apache License Copyright 2023 Ozone Networks Inc.

Portions of this software include code from [Jetbrains Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)  which is licensed under the Apache License Version 2.0 and which is copyright 2020-2021 JetBrains s.r.o. and their respective authors and developers 
