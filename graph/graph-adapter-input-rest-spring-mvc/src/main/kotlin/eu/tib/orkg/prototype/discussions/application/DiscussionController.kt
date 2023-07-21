package eu.tib.orkg.prototype.discussions.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.domain.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.discussions.DiscussionCommentRepresentationAdapter
import eu.tib.orkg.prototype.discussions.api.CreateDiscussionCommentUseCase.CreateCommand
import eu.tib.orkg.prototype.discussions.api.DiscussionCommentRepresentation
import eu.tib.orkg.prototype.discussions.api.DiscussionUseCases
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.application.UserNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.security.Principal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/discussions/", produces = [MediaType.APPLICATION_JSON_VALUE])
class DiscussionController(
    private val service: DiscussionUseCases,
    private val userService: UserService
) : DiscussionCommentRepresentationAdapter {
    @PostMapping("/topic/{topic}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createDiscussionComment(
        @PathVariable topic: ThingId,
        @RequestBody @Valid request: CreateCommentRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        principal: Principal?
    ): ResponseEntity<DiscussionCommentRepresentation> {
        if (principal?.name == null)
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val userId = UUID.fromString(principal.name)
        val user = userService.findById(userId).orElseThrow { UserNotFound(userId) }
        val comment = service.create(
            CreateCommand(
                topic = topic,
                message = request.message,
                createdBy = ContributorId(user.id)
            )
        )
        val location = uriComponentsBuilder
            .path("api/discussions/topic/{topic}/{comment}")
            .buildAndExpand(topic, comment)
            .toUri()
        return created(location).body(
            service.findByTopicAndCommentId(topic, comment).mapToDiscussionCommentRepresentation().get()
        )
    }

    @GetMapping("/topic/{topic}")
    fun findDiscussionComments(
        @PathVariable topic: ThingId,
        pageable: Pageable
    ): Page<DiscussionCommentRepresentation> =
        service.findAllByTopic(topic, pageable).mapToDiscussionCommentRepresentation()

    @GetMapping("/topic/{topic}/{comment}")
    fun findDiscussionComment(
        @PathVariable topic: ThingId,
        @PathVariable comment: DiscussionCommentId
    ): DiscussionCommentRepresentation =
        service.findByTopicAndCommentId(topic, comment)
            .mapToDiscussionCommentRepresentation()
            .orElseThrow { CommentNotFound(comment) }

    @DeleteMapping("/topic/{topic}/{comment}")
    fun deleteDiscussionComment(
        @PathVariable topic: ThingId,
        @PathVariable comment: DiscussionCommentId,
        principal: Principal?
    ): ResponseEntity<Any> {
        if (principal?.name == null)
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        val userId = UUID.fromString(principal.name)
        service.delete(ContributorId(userId), topic, comment)
        return noContent().build()
    }

    data class CreateCommentRequest(
        @NotBlank
        @JsonProperty("message")
        val message: String
    )
}
