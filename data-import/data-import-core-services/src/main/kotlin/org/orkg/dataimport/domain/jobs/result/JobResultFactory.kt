package org.orkg.dataimport.domain.jobs.result

import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.springframework.batch.core.job.JobExecution
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
class JobResultFactory(
    private val objectMapper: ObjectMapper,
    resultformatters: List<JobResultFormatter>,
) {
    private val jobNameToResultFormatter =
        resultformatters.flatMap { formatter -> formatter.jobNames().map { it to formatter } }.toMap()

    fun getResult(jobExecution: JobExecution, status: Status, pageable: Pageable): Optional<Any> {
        val formatter = jobNameToResultFormatter[jobExecution.jobInstance.jobName]
            ?: JobResultFormatter.DEFAULT
        return formatter.getResult(jobExecution, status, pageable, objectMapper)
    }
}
