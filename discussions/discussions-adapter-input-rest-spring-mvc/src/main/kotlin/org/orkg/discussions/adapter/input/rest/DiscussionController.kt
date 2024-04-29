package org.orkg.discussions.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.security.Principal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.community.output.ContributorRepository
import org.orkg.discussions.adapter.input.rest.mapping.DiscussionCommentRepresentationAdapter
import org.orkg.discussions.domain.CommentNotFound
import org.orkg.discussions.domain.DiscussionCommentId
import org.orkg.discussions.input.CreateDiscussionCommentUseCase.CreateCommand
import org.orkg.discussions.input.DiscussionUseCases
import org.orkg.graph.domain.UserNotFound
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
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
    private val userService: ContributorRepository,
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
        val contributorId = ContributorId(UUID.fromString(principal.name))
        val contributor = userService.findById(contributorId).orElseThrow { UserNotFound(contributorId.value) }
        val comment = service.create(
            CreateCommand(
                topic = topic,
                message = request.message,
                createdBy = contributor.id,
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
        @field:NotBlank
        @JsonProperty("message")
        val message: String
    )
}
