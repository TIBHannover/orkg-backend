package eu.tib.orkg.prototype.contributions.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.contributions.application.json.ContributorIdDeserializer
import eu.tib.orkg.prototype.contributions.application.json.ContributorIdSerializer
import java.util.UUID

@JsonDeserialize(using = ContributorIdDeserializer::class)
@JsonSerialize(using = ContributorIdSerializer::class)
data class ContributorId(val value: UUID) {
    companion object {
        fun createUnknownContributor() = ContributorId(UUID(0, 0))
    }

    override fun toString() = value.toString()
}
