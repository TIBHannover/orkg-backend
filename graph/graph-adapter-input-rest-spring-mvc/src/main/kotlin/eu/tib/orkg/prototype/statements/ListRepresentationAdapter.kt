package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ListRepresentation
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface ListRepresentationAdapter {

    fun Optional<List>.mapToListRepresentation(): Optional<ListRepresentation> =
        map { it.toListRepresentation() }

    fun Page<List>.mapToListRepresentation(): Page<ListRepresentation> =
        map { it.toListRepresentation() }

    fun List.toListRepresentation(): ListRepresentation =
        object : ListRepresentation {
            override val id: ThingId = this@toListRepresentation.id
            override val label: String = this@toListRepresentation.label
            override val elements: kotlin.collections.List<ThingId> = this@toListRepresentation.elements
            override val createdAt: OffsetDateTime = this@toListRepresentation.createdAt
            override val createdBy: ContributorId = this@toListRepresentation.createdBy
            override val jsonClass: String = "list"
        }
}
