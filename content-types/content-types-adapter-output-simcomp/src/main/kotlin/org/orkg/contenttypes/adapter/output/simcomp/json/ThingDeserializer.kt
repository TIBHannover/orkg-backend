package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

class ThingDeserializer : JsonDeserializer<Thing>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): Thing = with(p!!.codec) {
        val node = readTree<JsonNode>(p)
        when (val type = node["_class"].asText()) {
            "resource" -> treeToValue(node, Resource::class.java)
            "literal" -> treeToValue(node, Literal::class.java)
            "class" -> treeToValue(node, Class::class.java)
            "predicate" -> treeToValue(node, Predicate::class.java)
            else -> throw JsonMappingException(p, """Invalid value "$type" for property "_class".""")
        }
    }
}
