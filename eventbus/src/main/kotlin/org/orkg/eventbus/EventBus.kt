package org.orkg.eventbus

interface EventBus {
    fun register(listener: Listener)

    fun post(event: Event)
}
