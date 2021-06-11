package eu.tib.orkg.prototype.events.processor

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.lang.reflect.Method


@Component
class EventSubscribeBeanPostProcessor(
    var eventBus: EventBus
) : BeanPostProcessor {
    @Throws(BeansException::class)
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        return bean
    }

     @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val methods: Array<Method> = bean.javaClass.methods
        for (method in methods) {
            val annotations: Array<Annotation> = method.annotations
            for (annotation in annotations) {
                if (annotation.annotationClass == Subscribe::class.java) {
                    eventBus.register(bean)
                    return bean
                }
            }
        }
        return bean
    }
}
