package org.orkg.eventbus

fun interface Listener {
    fun notify(event: Event)
}
