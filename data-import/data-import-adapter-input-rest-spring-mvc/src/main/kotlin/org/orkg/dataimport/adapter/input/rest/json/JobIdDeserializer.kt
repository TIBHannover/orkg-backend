package org.orkg.dataimport.adapter.input.rest.json

import org.orkg.dataimport.domain.jobs.JobId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class JobIdDeserializer : ValueDeserializer<JobId>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): JobId? =
        p?.valueAsLong?.let(::JobId)
}
