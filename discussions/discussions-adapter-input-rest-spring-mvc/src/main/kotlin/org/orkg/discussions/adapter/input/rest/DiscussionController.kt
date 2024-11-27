package org.orkg.discussions.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.discussions.adapter.input.rest.mapping.DiscussionCommentRepresentationAdapter
import org.orkg.discussions.domain.CommentNotFound
import org.orkg.discussions.domain.DiscussionCommentId
import org.orkg.discussions.input.CreateDiscussionCommentUseCase.CreateCommand
import org.orkg.discussions.input.DiscussionUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/discussions", produces = [MediaType.APPLICATION_JSON_VALUE])
class DiscussionController(
    private val service: DiscussionUseCases,
) : DiscussionCommentRepresentationAdapter {
    @RequireLogin
    @PostMapping("/topic/{topic}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createDiscussionComment(
        @PathVariable topic: ThingId,
        @RequestBody @Valid request: CreateCommentRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?
    ): ResponseEntity<DiscussionCommentRepresentation> {
        val userId = currentUser.contributorId()
        val comment = service.create(
            CreateCommand(
                topic = topic,
                message = request.message,
                createdBy = userId,
            )
        )
        val location = uriComponentsBuilder
            .path("/api/discussions/topic/{topic}/{comment}")
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

    @RequireLogin
    @DeleteMapping("/topic/{topic}/{comment}")
    fun deleteDiscussionComment(
        @PathVariable topic: ThingId,
        @PathVariable comment: DiscussionCommentId,
        currentUser: Authentication?
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.delete(userId, topic, comment)
        return noContent().build()
    }

    data class CreateCommentRequest(
        @field:NotBlank
        @JsonProperty("message")
        val message: String
    )
}
