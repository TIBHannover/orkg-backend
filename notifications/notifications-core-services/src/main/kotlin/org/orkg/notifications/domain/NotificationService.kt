package org.orkg.notifications.domain

import freemarker.core.HTMLOutputFormat
import freemarker.template.Configuration
import freemarker.template.Configuration.VERSION_2_3_34
import freemarker.template.Template
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import org.orkg.notifications.domain.internal.EmailTemplateLoader
import org.orkg.notifications.domain.internal.MessageFormatter
import org.orkg.notifications.input.NotificationUseCases
import org.orkg.notifications.output.EmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.StringWriter

@Service
class NotificationService(
    private val emailService: EmailService,
    private val emailTemplateLoader: EmailTemplateLoader,
) : NotificationUseCases {
    private val emailTemplateCache = mutableMapOf<String, Template>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendTestEmail(recipient: Recipient, message: String) {
        val model = mapOf(
            "message" to message
        )
        sendEmail(recipient, "email-test-subject", "email-test", model)
    }

    private fun sendEmail(recipient: Recipient, subjectKey: String, templateName: String, model: Map<String, Any>) {
        val htmlModel = model.toMutableMap()
        val textModel = model.toMutableMap()
        htmlModel["formatMessage"] = MessageFormatter(emailTemplateLoader.messages, ::escapeHtml4)
        textModel["formatMessage"] = MessageFormatter(emailTemplateLoader.messages)
        val htmlBody = getEmailTemplate("html/$templateName.ftl").render(htmlModel).replace("\n", "")
        val textBody = getEmailTemplate("text/$templateName.ftl").render(textModel)
        val subject = emailTemplateLoader.messages[subjectKey] ?: throw IllegalArgumentException("Unknown subject key: $subjectKey")
        val email = Email(subject, htmlBody, textBody)
        try {
            emailService.send(recipient, email)
        } catch (t: Throwable) {
            logger.error("""Error sending email. Subject: "{}", Template: "{}", Model: {}.""", subjectKey, templateName, model, t)
        }
    }

    private fun getEmailTemplate(templateName: String): Template =
        emailTemplateCache.getOrPut(templateName) {
            val config = Configuration(VERSION_2_3_34).apply {
                outputFormat = HTMLOutputFormat.INSTANCE
                templateLoader = emailTemplateLoader
            }
            config.getTemplate(templateName, Charsets.UTF_8.name())
        }

    private fun Template.render(model: Any): String =
        StringWriter().apply { process(model, this) }.toString()
}
