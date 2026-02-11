package org.orkg.notifications.domain.internal

import freemarker.cache.URLTemplateLoader
import org.springframework.stereotype.Component
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.readValue
import java.net.URL

@Component
class EmailTemplateLoader : URLTemplateLoader() {
    val messages = loadEmailMessages()

    override fun getURL(name: String?): URL? =
        javaClass.classLoader.getResource("assets/notifications/email/$name")

    private fun loadEmailMessages(): Map<String, String> =
        YAMLMapper().readValue<Map<String, String>>(getURL("messages/messages.yaml")!!.openStream())
}
