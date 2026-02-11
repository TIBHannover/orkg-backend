package org.orkg.graph.adapter.input.rest.json

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.time.OffsetDateTime

class ResourceDeserializer : ValueDeserializer<Resource>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Resource {
        val node = ctxt.readTree(p)
        return Resource(
            id = ThingId(node["id"].asString()),
            label = node["label"].asString(),
            createdAt = OffsetDateTime.parse(node["created_at"].asString()),
            classes = node["classes"].map { ThingId(it.asString()) }.toSet(),
            createdBy = ContributorId(node["created_by"].asString()),
            observatoryId = ObservatoryId(node["observatory_id"].asString()),
            extractionMethod = ExtractionMethod.valueOf(node["extraction_method"].asString()),
            organizationId = OrganizationId(node["organization_id"].asString()),
            visibility = node["visibility"]?.asString()?.let(Visibility::valueOf)
                ?: visibilityFromFlags(node["featured"]?.asBoolean(), node["unlisted"]?.asBoolean()),
            verified = node["verified"]?.asBoolean(),
            unlistedBy = node["unlisted_by"]?.stringValue(null)?.let(::ContributorId),
            modifiable = node["modifiable"]?.asBoolean() ?: true
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
