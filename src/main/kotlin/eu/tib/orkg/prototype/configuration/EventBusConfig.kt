package eu.tib.orkg.prototype.configuration

import com.google.common.eventbus.EventBus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventBusConfig {
    //Define the event bus bean
    @Bean
    fun eventBus(): EventBus {
        return EventBus()
    }
}
