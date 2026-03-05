package org.orkg.graph.domain

import org.orkg.common.ContributorId
import java.time.OffsetDateTime

data class ClassSubclassRelation(
    val child: Class,
    val parent: Class,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
)
