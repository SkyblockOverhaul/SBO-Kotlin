package net.sbo.mod.utils.events.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)


/**
 * Marks a function as a listener for the Sbo event bus.
 *
 * The annotated function:
 * - Must have exactly one parameter, representing the event type.
 * Events are organized in the following packages:
 * - [net.sbo.mod.utils.events.impl.entity]
 * - [net.sbo.mod.utils.events.impl.game]
 * - [net.sbo.mod.utils.events.impl.guis]
 * - [net.sbo.mod.utils.events.impl.packets]
 * - [net.sbo.mod.utils.events.impl.partyfinder]
 * - [net.sbo.mod.utils.events.impl.render]
 * - Must be defined inside an `object` singleton or a `companion object`.
 *
 * The function is automatically registered with the event bus at compile time
 * and is invoked whenever the corresponding event is fired.
 *
 * Example usage:
 * ```kotlin
 * @SboEvent
 * fun onSomeEvent(event: SomeEvent) {
 * // handle the event here
 * }
 * ```
 *
 * See [net.sbo.mod.processor.SboEventProcessor] for details about the compile-time processor.
 */
annotation class SboEvent