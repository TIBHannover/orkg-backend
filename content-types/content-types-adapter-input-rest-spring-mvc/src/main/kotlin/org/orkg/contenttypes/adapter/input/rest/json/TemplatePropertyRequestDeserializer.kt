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

class TemplatePropertyRequestDeserializer : JsonDeserializer<TemplatePropertyRequest>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): TemplatePropertyRequest = with(p!!.codec) {
        val node = readTree<JsonNode>(p)
        when {
            node.has("datatype") -> when {
                node.has("pattern") -> treeToValue(node, StringLiteralPropertyRequest::class.java)
                node.has("min_inclusive") || node.has("max_inclusive") -> {
                    treeToValue(node, NumberLiteralPropertyRequest::class.java)
                }
                else -> treeToValue(node, OtherLiteralPropertyRequest::class.java)
            }
            node.has("class") -> treeToValue(node, ResourcePropertyRequest::class.java)
            else -> treeToValue(node, UntypedPropertyRequest::class.java)
        }
    }
}
