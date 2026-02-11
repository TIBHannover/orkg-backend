package org.orkg.dataimport.adapter.input.rest.json

import org.orkg.dataimport.domain.jobs.JobId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class JobIdSerializer : ValueSerializer<JobId>() {
    override fun serialize(value: JobId?, gen: JsonGenerator?, serializers: SerializationContext?) {
        gen?.writeString(value.toString())
    }
}
