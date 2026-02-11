package org.orkg.contenttypes.adapter.output.simcomp

import org.junit.jupiter.api.Test
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.adapter.output.simcomp.json.SimCompJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleDeserializers
import tools.jackson.databind.module.SimpleModule

internal class JacksonUnitTest : MockkBaseTest {
    private val jsonMapper = JsonMapper.builder()
        .findAndAddModules()
        .addModules(CommonJacksonModule(), GraphJacksonModule(), SimCompJacksonModule())
        .addModule(
            object : SimpleModule() {
                override fun setupModule(context: SetupContext?) {
                    context!!.addDeserializers(
                        SimpleDeserializers().apply {
                            addDeserializer(Example::class.java, ExampleDeserializer())
                        }
                    )
                }
            }
        )
        .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .build()

    data class Example(
        val foo: String,
        val bar: String,
    )

    class ExampleDeserializer : ValueDeserializer<Example>() {
        override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): Example? {
            val tree = jsonParser.readValueAsTree<JsonNode>()
            println(tree)
            return Example(
                tree["foo"].asString(),
                tree["bar"].asString(),
            )
        }
    }

    @Test
    fun test() {
        val read = jsonMapper.readValue(
            """
            {
                "foo": "a",
                "foo1": "a",
                "bar1": "b",
                "foo2": "a",
                "_stop": "stop",
                "bar2": "b",
                "bar": "b"
            }
            """,
            Example::class.java
        )
        println(read)
    }
}
