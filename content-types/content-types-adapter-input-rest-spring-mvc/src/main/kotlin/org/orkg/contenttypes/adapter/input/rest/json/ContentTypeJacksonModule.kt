package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.contenttypes.adapter.input.rest.IdentifierMapRequest
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRequest
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.legacy.ComparisonHeaderCell
import org.orkg.contenttypes.domain.legacy.ComparisonIndexCell
import org.orkg.contenttypes.domain.legacy.ComparisonTargetCell
import org.orkg.contenttypes.domain.legacy.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.graph.domain.DynamicLabel
import tools.jackson.databind.module.SimpleDeserializers
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.module.SimpleSerializers

class ContentTypeJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(IdentifierMapRequest::class.java, IdentifierMapRequestDeserializer())
                addDeserializer(TemplatePropertyRequest::class.java, TemplatePropertyRequestDeserializer())
                addDeserializer(TemplatePropertyRepresentation::class.java, TemplatePropertyRepresentationDeserializer())
                addDeserializer(SnapshotId::class.java, SnapshotIdDeserializer())
                addDeserializer(DynamicLabel::class.java, DynamicLabelDeserializer())
            }
        )
        context?.addSerializers(
            SimpleSerializers().apply {
                addSerializer(IdentifierMapRequest::class.java, IdentifierMapRequestSerializer())
                addSerializer(SnapshotId::class.java, SnapshotIdSerializer())
                addSerializer(DynamicLabel::class.java, DynamicLabelSerializer())
            }
        )
        context?.setMixIn(LegacyComparisonConfig::class.java, ComparisonConfigMixin::class.java)
        context?.setMixIn(ComparisonHeaderCell::class.java, ComparisonHeaderCellMixin::class.java)
        context?.setMixIn(ComparisonIndexCell::class.java, ComparisonIndexCellMixin::class.java)
        context?.setMixIn(ComparisonTargetCell::class.java, ComparisonTargetCellMixin::class.java)
        context?.setMixIn(ConfiguredComparisonTargetCell::class.java, ConfiguredComparisonTargetCellMixin::class.java)
    }
}
