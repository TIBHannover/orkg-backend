package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.common.treeToValue
import org.orkg.contenttypes.adapter.input.rest.NumberLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.OtherLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.ResourceTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.StringLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.UntypedTemplatePropertyRepresentation
import org.orkg.graph.domain.Literals
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer

class TemplatePropertyRepresentationDeserializer : ValueDeserializer<TemplatePropertyRepresentation>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): TemplatePropertyRepresentation {
        val node = ctxt.readTree(p)
        return when {
            node.has("datatype") -> when {
                isStringLiteralNode(node) -> p.treeToValue(node, StringLiteralTemplatePropertyRepresentation::class)
                isNumberLiteralNode(node) -> p.treeToValue(node, NumberLiteralTemplatePropertyRepresentation::class)
                else -> p.treeToValue(node, OtherLiteralTemplatePropertyRepresentation::class)
            }

            node.has("class") -> p.treeToValue(node, ResourceTemplatePropertyRepresentation::class)

            else -> p.treeToValue(node, UntypedTemplatePropertyRepresentation::class)
        }
    }

    private fun isStringLiteralNode(node: JsonNode): Boolean =
        node.has("pattern") || node.get("datatype").path("id").asString() == Literals.XSD.STRING.`class`.value

    private fun isNumberLiteralNode(node: JsonNode): Boolean =
        node.has("min_inclusive") ||
            node.has("max_inclusive") ||
            node.get("datatype").path("id").asString() == Literals.XSD.DECIMAL.`class`.value ||
            node.get("datatype").path("id").asString() == Literals.XSD.FLOAT.`class`.value ||
            node.get("datatype").path("id").asString() == Literals.XSD.INT.`class`.value
}
