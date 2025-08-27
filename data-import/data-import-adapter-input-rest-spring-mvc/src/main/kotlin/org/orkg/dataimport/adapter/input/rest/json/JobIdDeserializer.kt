package org.orkg.dataimport.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.dataimport.domain.jobs.JobId

class JobIdDeserializer : JsonDeserializer<JobId>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): JobId? =
        p?.valueAsLong?.let(::JobId)
}
