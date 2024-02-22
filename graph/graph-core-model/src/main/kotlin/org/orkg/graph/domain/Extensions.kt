package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId

val Thing.createdAt: OffsetDateTime
    get() = when (this) {
        is Resource -> createdAt
        is Class -> createdAt
        is Predicate -> createdAt
        is Literal -> createdAt
    }

val Thing.createdBy: ContributorId
    get() = when (this) {
        is Resource -> createdBy
        is Class -> createdBy
        is Predicate -> createdBy
        is Literal -> createdBy
    }
