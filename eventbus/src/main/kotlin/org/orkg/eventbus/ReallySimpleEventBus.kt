package org.orkg.eventbus

import org.springframework.stereotype.Service

/**
 * A really simple event bus.
 * It is synchronous, not thread-safe, and runs into issues if a message does not get delivered or the listener throws
 *  an exception.
 */
@Service
class ReallySimpleEventBus : EventBus {
    private val listeners: MutableSet<Listener> = mutableSetOf()

    override fun register(listener: Listener) {
        listeners.add(listener)
    }

    override fun post(event: Event) {
        listeners.forEach { listener ->
            listener.notify(event)
        }
    }
}
