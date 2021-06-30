package eu.tib.orkg.prototype.events.cron

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.events.service.NotificationUpdatesService
import eu.tib.orkg.prototype.statements.domain.model.StatsService
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.thymeleaf.spring5.SpringTemplateEngine
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.mail.internet.MimeMessage

@Service
class SchedulerService(
    private val templateEngine: SpringTemplateEngine,
    private val emailSender: JavaMailSender,
    private val service: NotificationUpdatesService,
    private val userService: UserService,
    private val statsService: StatsService
) {

    //@Scheduled(cron= "0 18 * * 1-5")
    @Scheduled(cron = "0/3 * * * * *")
    fun cronScheduledTask() {
        println("Testing cron email...")
        val notificationStatistics = service.getTotalResourcesByGroupOfAllUsers()
        notificationStatistics.map { it ->
            if (it.getCount() > 0) {
                val user = userService.findById(UUID.fromString(it.getUserId()))
                val trendingPapers = statsService.getTopTrendingPapers()

                var mimeMessage: MimeMessage = emailSender.createMimeMessage()

                var helper = MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets
                        .UTF_8.name()
                )

                var context: org.thymeleaf.context.Context = org.thymeleaf.context.Context()
                context.setVariable("papers", it.getCount())
                context.setVariable("name", user.get().displayName)
                context.setVariable("trendingPapers", trendingPapers)
                var html: String = templateEngine.process(
                    "mail-template.html",
                    context
                )
                helper.setTo(user.get().email)
                helper.setText(html, true)
                helper.setSubject("ORKG Digest")
                helper.setFrom("no-reply@orkg.org")
                emailSender.send(mimeMessage)
            }
        }

    }
}
