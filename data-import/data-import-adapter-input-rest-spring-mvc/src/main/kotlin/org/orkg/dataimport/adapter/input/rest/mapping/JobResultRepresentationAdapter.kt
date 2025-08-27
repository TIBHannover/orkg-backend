package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.domain.jobs.JobResult
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import java.util.Optional

interface JobResultRepresentationAdapter {
    val jobResultRepresentationFactory: JobResultRepresentationFactory

    fun Optional<JobResult>.mapToJobResultRepresentation(): Optional<ResponseEntity<Any>> =
        map { it.toJobResultRepresentation() }

    fun JobResult.toJobResultRepresentation(): ResponseEntity<Any> {
        val result = jobResultRepresentationFactory.getResultRepresentation(this)
        if (result == null) {
            return noContent().build()
        }
        val contentType = """application/vnd.orkg.job.$jobName.${status.name.lowercase()}+json"""
        return ok().contentType(MediaType.valueOf(contentType)).body(result)
    }
}
