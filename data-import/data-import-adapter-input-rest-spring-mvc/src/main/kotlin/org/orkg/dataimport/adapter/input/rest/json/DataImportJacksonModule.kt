package org.orkg.dataimport.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.dataimport.domain.jobs.JobId

class DataImportJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        context.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(JobId::class.java, JobIdDeserializer())
            }
        )
        context.addSerializers(
            SimpleSerializers().apply {
                addSerializer(JobId::class.java, JobIdSerializer())
            }
        )
    }
}
