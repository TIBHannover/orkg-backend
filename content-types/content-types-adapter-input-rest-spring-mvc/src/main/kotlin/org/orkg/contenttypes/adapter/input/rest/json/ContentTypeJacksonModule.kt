package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.contenttypes.adapter.input.rest.IdentifierMapDTO
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRequest
import org.orkg.contenttypes.domain.ComparisonConfig
import org.orkg.contenttypes.domain.ComparisonHeaderCell
import org.orkg.contenttypes.domain.ComparisonIndexCell
import org.orkg.contenttypes.domain.ComparisonTargetCell
import org.orkg.contenttypes.domain.ConfiguredComparisonTargetCell

class ContentTypeJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(IdentifierMapDTO::class.java, IdentifierMapDTODeserializer())
            addDeserializer(TemplatePropertyRequest::class.java, TemplatePropertyRequestDeserializer())
            addDeserializer(TemplatePropertyRepresentation::class.java, TemplatePropertyRepresentationDeserializer())
        })
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(IdentifierMapDTO::class.java, IdentifierMapDTOSerializer())
        })
        context?.setMixInAnnotations(ComparisonConfig::class.java, ComparisonConfigMixin::class.java)
        context?.setMixInAnnotations(ComparisonHeaderCell::class.java, ComparisonHeaderCellMixin::class.java)
        context?.setMixInAnnotations(ComparisonIndexCell::class.java, ComparisonIndexCellMixin::class.java)
        context?.setMixInAnnotations(ComparisonTargetCell::class.java, ComparisonTargetCellMixin::class.java)
        context?.setMixInAnnotations(ConfiguredComparisonTargetCell::class.java, ConfiguredComparisonTargetCellMixin::class.java)
    }
}
