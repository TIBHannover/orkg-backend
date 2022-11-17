package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.OrganizationIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.OrganizationIdSerializer
import java.util.UUID

@JsonDeserialize(using = OrganizationIdDeserializer::class)
@JsonSerialize(using = OrganizationIdSerializer::class)
data class OrganizationId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        fun createUnknownOrganization() = OrganizationId(UUID(0, 0))
    }

    override fun toString() = value.toString()
}
