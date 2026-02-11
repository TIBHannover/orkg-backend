package org.orkg.dataimport.adapter.input.rest.json

import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import tools.jackson.databind.module.SimpleDeserializers
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.module.SimpleSerializers

class DataImportJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        context.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(CSVID::class.java, CSVIDDeserializer())
                addDeserializer(JobId::class.java, JobIdDeserializer())
            }
        )
        context.addSerializers(
            SimpleSerializers().apply {
                addSerializer(CSVID::class.java, CSVIDSerializer())
                addSerializer(JobId::class.java, JobIdSerializer())
            }
        )
    }
}
