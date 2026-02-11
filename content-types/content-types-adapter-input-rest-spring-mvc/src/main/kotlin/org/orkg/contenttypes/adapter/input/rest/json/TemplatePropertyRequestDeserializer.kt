package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.common.treeToValue
import org.orkg.contenttypes.adapter.input.rest.NumberLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.OtherLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.ResourcePropertyRequest
import org.orkg.contenttypes.adapter.input.rest.StringLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRequest
import org.orkg.contenttypes.adapter.input.rest.UntypedPropertyRequest
import org.orkg.graph.domain.Literals
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer

class TemplatePropertyRequestDeserializer : ValueDeserializer<TemplatePropertyRequest>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): TemplatePropertyRequest {
        val node = ctxt.readTree(p)
        return when {
            node.has("datatype") -> when {
                isStringLiteralNode(node) -> p.treeToValue(node, StringLiteralPropertyRequest::class)
                isNumberLiteralNode(node) -> p.treeToValue(node, NumberLiteralPropertyRequest::class)
                else -> p.treeToValue(node, OtherLiteralPropertyRequest::class)
            }

            node.has("class") -> p.treeToValue(node, ResourcePropertyRequest::class)

            else -> p.treeToValue(node, UntypedPropertyRequest::class)
        }
    }

    private fun isStringLiteralNode(node: JsonNode): Boolean =
        node.has("pattern") || node.get("datatype").asString() == Literals.XSD.STRING.`class`.value

    private fun isNumberLiteralNode(node: JsonNode): Boolean =
        node.has("min_inclusive") ||
            node.has("max_inclusive") ||
            node.get("datatype").asString() == Literals.XSD.DECIMAL.`class`.value ||
            node.get("datatype").asString() == Literals.XSD.FLOAT.`class`.value ||
            node.get("datatype").asString() == Literals.XSD.INT.`class`.value
}
