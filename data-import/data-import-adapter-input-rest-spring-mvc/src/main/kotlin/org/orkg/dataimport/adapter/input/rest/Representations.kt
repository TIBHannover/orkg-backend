package org.orkg.dataimport.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import java.time.OffsetDateTime

data class CSVRepresentation(
    val id: CSVID,
    val name: String,
    val type: CSV.Type,
    val format: CSV.Format,
    val state: CSV.State,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)

data class JobStatusRepresentation(
    @get:JsonProperty("job_id")
    val jobId: JobId,
    @get:JsonProperty("job_name")
    val jobName: String,
    val context: Map<String, Any?>,
    val status: Status,
)
