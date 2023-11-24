package org.orkg.discussions.domain

import java.util.*

data class DiscussionCommentId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    override fun toString() = value.toString()
}
