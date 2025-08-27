@file:Suppress("ktlint:standard:filename")

package org.orkg.dataimport.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobStatus.Status

data class JobStatusRepresentation(
    @get:JsonProperty("job_id")
    val jobId: JobId,
    @get:JsonProperty("job_name")
    val jobName: String,
    val context: Map<String, Any?>,
    val status: Status,
)
