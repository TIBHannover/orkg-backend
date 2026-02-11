package org.orkg.dataimport.domain.jobs.result

import org.orkg.dataimport.domain.JobException
import org.orkg.dataimport.domain.PROBLEMS
import org.orkg.dataimport.domain.getAndCast
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.springframework.batch.core.job.JobExecution
import org.springframework.data.domain.Pageable
import org.springframework.http.ProblemDetail
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.Optional

interface JobResultFormatter {
    fun getResult(jobExecution: JobExecution, status: Status, pageable: Pageable, objectMapper: ObjectMapper): Optional<Any> =
        if (status == Status.FAILED) {
            val problemDetails = jobExecution.executionContext.getAndCast<ByteArray>(PROBLEMS)
                ?.let { objectMapper.readValue<List<ProblemDetail>>(it) }
                .orEmpty()
            throw JobException(problemDetails)
        } else {
            Optional.empty()
        }

    fun jobNames(): Set<String>

    companion object {
        val DEFAULT = object : JobResultFormatter {
            override fun jobNames(): Set<String> = emptySet()
        }
    }
}
