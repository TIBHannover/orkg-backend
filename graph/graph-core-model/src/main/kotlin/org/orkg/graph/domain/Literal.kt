package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

data class Literal(
    override val id: ThingId,
    override val label: String,
    val datatype: String = "xsd:string",
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId = ContributorId.UNKNOWN,
    override val modifiable: Boolean = true
) : Thing
