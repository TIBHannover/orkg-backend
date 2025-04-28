package org.orkg.notifications.adapter.input.rest

import jakarta.validation.Valid
import org.orkg.common.annotations.RequireAdminRole
import org.orkg.notifications.domain.Recipient
import org.orkg.notifications.input.NotificationUseCases
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/emails")
class EmailController(
    private val notificationUseCases: NotificationUseCases,
) {
    @PostMapping("/test", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireAdminRole
    fun sendTestEmail(
        @RequestBody @Valid request: SendTestEmailRequest,
        currentUser: JwtAuthenticationToken,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Any> {
        val jwt = currentUser.token as Jwt
        val email = jwt.getClaim<String>("email")!!
        val name = jwt.getClaim<String>("preferred_username")!!
        notificationUseCases.sendTestEmail(
            recipient = Recipient(email, name),
            message = request.message
        )
        return noContent().build()
    }

    data class SendTestEmailRequest(
        val message: String,
    )
}
