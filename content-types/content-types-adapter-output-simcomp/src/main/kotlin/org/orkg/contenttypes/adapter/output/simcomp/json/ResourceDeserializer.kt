package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import java.time.OffsetDateTime

class ResourceDeserializer : JsonDeserializer<Resource>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): Resource = with(p!!.codec.readTree<JsonNode>(p)) {
        Resource(
            id = ThingId(this["id"].asText()),
            label = this["label"].asText(),
            createdAt = OffsetDateTime.parse(this["created_at"].asText()),
            classes = this["classes"].map { ThingId(it.asText()) }.toSet(),
            createdBy = ContributorId(this["created_by"].asText()),
            observatoryId = ObservatoryId(this["observatory_id"].asText()),
            extractionMethod = ExtractionMethod.valueOf(this["extraction_method"].asText()),
            organizationId = OrganizationId(this["organization_id"].asText()),
            visibility = this["visibility"]?.asText()?.let(Visibility::valueOf)
                ?: visibilityFromFlags(this["featured"]?.asBoolean(), this["unlisted"]?.asBoolean()),
            verified = this["verified"]?.asBoolean(),
            unlistedBy = this["unlisted_by"]?.asText()?.let(::ContributorId),
            modifiable = this["modifiable"]?.asBoolean() ?: true
        )
    }

    private fun visibilityFromFlags(featured: Boolean?, unlisted: Boolean?): Visibility =
        when (unlisted) {
            true -> Visibility.UNLISTED
            else -> when (featured) {
                true -> Visibility.FEATURED
                else -> Visibility.DEFAULT
            }
        }
}
