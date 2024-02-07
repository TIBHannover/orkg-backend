package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

data class Literal(
    override val id: ThingId,
    override val label: String,
    val datatype: String = "xsd:string",
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
    val modifiable: Boolean = true
) : Thing
