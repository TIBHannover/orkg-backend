package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.orkg.contenttypes.adapter.input.rest.NumberLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.OtherLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.ResourceTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.StringLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.UntypedTemplatePropertyRepresentation

class TemplatePropertyRepresentationDeserializer : JsonDeserializer<TemplatePropertyRepresentation>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): TemplatePropertyRepresentation = with(p!!.codec) {
        val node = readTree<JsonNode>(p)
        when {
            node.has("datatype") -> when {
                node.has("pattern") -> treeToValue(node, StringLiteralTemplatePropertyRepresentation::class.java)
                node.has("min_inclusive") || node.has("max_inclusive") -> {
                    treeToValue(node, NumberLiteralTemplatePropertyRepresentation::class.java)
                }
                else -> treeToValue(node, OtherLiteralTemplatePropertyRepresentation::class.java)
            }
            node.has("class") -> treeToValue(node, ResourceTemplatePropertyRepresentation::class.java)
            else -> treeToValue(node, UntypedTemplatePropertyRepresentation::class.java)
        }
    }
}
