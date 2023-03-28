package eu.tib.orkg.prototype.discussions.application

import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.shared.SimpleMessageException
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.http.HttpStatus

class TopicNotFound(val id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Topic "$id" not found.""")

class CommentNotFound(val id: DiscussionCommentId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Comment "$id" not found.""")

class InvalidContent :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Invalid message contents.""")

class Unauthorized :
    SimpleMessageException(HttpStatus.UNAUTHORIZED, """Unauthorized.""")
