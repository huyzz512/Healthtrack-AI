# ADR-002: Native Android with Kotlin and Compose

## Status

Accepted.

## Context

Health Connect, device permissions, background work, and notifications require strong Android platform integration.

## Decision

Use native Kotlin, Jetpack Compose, Material 3, MVVM/UDF, Hilt, coroutines, and a single Foundation `:app` module with package-based boundaries.

## Alternatives

- Cross-platform UI: rejected because it is outside the approved stack.
- Immediate Gradle multi-module decomposition: deferred to avoid early complexity.

## Consequences

Android-specific tools remain first class. Package boundaries must be enforced until scale justifies a reviewed modularization ADR.

## Impact

Android Studio and Gradle builds are authoritative; VS Code remains optional.

