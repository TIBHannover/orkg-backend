package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.orkg.contenttypes.adapter.input.rest.TemplateController.NumberLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.TemplateController.OtherLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.TemplateController.ResourcePropertyRequest
import org.orkg.contenttypes.adapter.input.rest.TemplateController.StringLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.TemplateController.TemplatePropertyRequest
import org.orkg.contenttypes.adapter.input.rest.TemplateController.UntypedPropertyRequest
import org.orkg.graph.domain.Literals

class TemplatePropertyRequestDeserializer : JsonDeserializer<TemplatePropertyRequest>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): TemplatePropertyRequest = with(p!!.codec) {
        val node = readTree<JsonNode>(p)
        when {
            node.has("datatype") -> when {
                isStringLiteralNode(node) -> treeToValue(node, StringLiteralPropertyRequest::class.java)
                isNumberLiteralNode(node) -> treeToValue(node, NumberLiteralPropertyRequest::class.java)
                else -> treeToValue(node, OtherLiteralPropertyRequest::class.java)
            }
            node.has("class") -> treeToValue(node, ResourcePropertyRequest::class.java)
            else -> treeToValue(node, UntypedPropertyRequest::class.java)
        }
    }

    private fun isStringLiteralNode(node: JsonNode): Boolean =
        node.has("pattern") || node.get("datatype").asText() == Literals.XSD.STRING.`class`.value

    private fun isNumberLiteralNode(node: JsonNode): Boolean =
        node.has("min_inclusive") || node.has("max_inclusive") ||
            node.get("datatype").asText() == Literals.XSD.DECIMAL.`class`.value ||
            node.get("datatype").asText() == Literals.XSD.FLOAT.`class`.value ||
            node.get("datatype").asText() == Literals.XSD.INT.`class`.value
}
