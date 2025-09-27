package net.sbo.mod.utils.events.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)


/**
 * Marks a function as a listener for the Sbo event bus.
 *
 * The annotated function:
 * - Must have exactly one parameter, representing the event type.
 *   All event types can be found in [net.sbo.mod.utils.events.impl].
 * - Must be defined inside an `object` singleton or a `companion object`.
 *
 * The function is automatically registered with the event bus at compile time
 * and is invoked whenever the corresponding event is fired.
 *
 * Example usage:
 * ```kotlin
 * @SboEvent
 * fun onSomeEvent(event: SomeEvent) {
 *     // handle the event here
 * }
 * ```
 *
 * See [net.sbo.mod.processor.SboEventProcessor] for details about the compile-time processor.
 */
annotation class SboEvent