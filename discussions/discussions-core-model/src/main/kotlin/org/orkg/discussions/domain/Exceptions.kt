package org.orkg.discussions.domain

import org.orkg.common.ThingId
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class TopicNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Topic "$id" not found.""")

class CommentNotFound(id: DiscussionCommentId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Comment "$id" not found.""")

class InvalidContent :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Invalid message contents.""")
