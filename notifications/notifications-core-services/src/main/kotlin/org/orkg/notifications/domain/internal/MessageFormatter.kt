package org.orkg.notifications.domain.internal

import freemarker.template.TemplateMethodModelEx
import org.slf4j.LoggerFactory
import java.text.MessageFormat

class MessageFormatter(
    private val messages: Map<String, String>,
    private val messageFilter: (String) -> String = { it },
) : TemplateMethodModelEx {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun exec(arguments: MutableList<Any?>): Any? {
        if (arguments.isNotEmpty()) {
            val key = arguments.first()?.toString()
            if (key == null) {
                return null
            }
            val message = messages[key]
            if (message == null) {
                logger.error("""Unknown message key "$key.""")
                return null
            }
            val templateArguments = arguments.subList(1, arguments.size)
                .map { it?.toString()?.let { messageFilter(it) } }
                .toTypedArray()
            return MessageFormat(message).format(templateArguments)
        }
        return null
    }
}
