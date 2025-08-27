package org.orkg.dataimport.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.dataimport.domain.jobs.JobId

class JobIdSerializer : JsonSerializer<JobId>() {
    override fun serialize(value: JobId?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value.toString())
    }
}
