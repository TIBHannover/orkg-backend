package org.orkg.notifications.adapter.output.mail

import jakarta.mail.internet.InternetAddress
import org.orkg.notifications.adapter.output.mail.configuration.EmailServiceConfiguration
import org.orkg.notifications.domain.Email
import org.orkg.notifications.domain.Recipient
import org.orkg.notifications.output.EmailService
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailServiceAdapter(
    private val configuration: EmailServiceConfiguration,
    private val mailSender: JavaMailSender,
) : EmailService {
    override fun send(recipient: Recipient, email: Email) {
        val message = mailSender.createMimeMessage()
        MimeMessageHelper(message, true).apply {
            setFrom(configuration.from.email, configuration.from.displayName)
            setTo(recipient.toInternetAddress(encoding))
            setSubject(email.subject)
            setText(email.textBody, email.htmlBody)
            val replyTo = configuration.replyTo
            if (!replyTo.email.isNullOrBlank()) {
                if (replyTo.displayName.isNullOrBlank()) {
                    setReplyTo(replyTo.email)
                } else {
                    setReplyTo(replyTo.email, replyTo.displayName)
                }
            }
        }
        mailSender.send(message)
    }

    private fun Recipient.toInternetAddress(encoding: String? = null): InternetAddress =
        InternetAddress(email, name, encoding)
}
