package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.graph.domain.StatementId

class GraphJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(StatementId::class.java, StatementIdSerializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(StatementId::class.java, StatementIdDeserializer())
        })
    }
}
