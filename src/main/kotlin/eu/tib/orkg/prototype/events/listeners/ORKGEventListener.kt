package eu.tib.orkg.prototype.events.listeners

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class ORKGEventListener {
    private val logger = Logger.getLogger("Event Listener")

    constructor(eventBus: EventBus){
        eventBus.register(this)
    }

    @Subscribe
    fun listener(event: NotificationData) {
        logger.info("New ${event.type} has been added: ${event.data}")
    }
}
