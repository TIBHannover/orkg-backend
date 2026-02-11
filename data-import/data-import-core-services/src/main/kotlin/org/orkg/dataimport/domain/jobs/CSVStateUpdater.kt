package org.orkg.dataimport.domain.jobs

import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener

class CSVStateUpdater(
    private val csvRepository: CSVRepository,
    private val startState: State,
    private val successState: State,
    private val stoppedState: State,
    private val failureState: State,
    private val jobIdSetter: (CSV, JobId) -> CSV,
) : JobExecutionListener {
    init {
        require(successState isAfter startState) { "successState must be after startState" }
        require(stoppedState isAfter startState) { "stoppedState must be after startState" }
        require(failureState isAfter startState) { "failureState must be after startState" }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        val id = extractCSVID(jobExecution)
        val jobId = JobId(jobExecution.id)
        val csv = csvRepository.findById(id)
            .orElseThrow { CSVNotFound(id) }
            .let { jobIdSetter(it, jobId) }

        check(!csv.state.isFinal) { """The state "${csv.state}" of CSV "${csv.id}" is final.""" }

        if (startState != csv.state) {
            csvRepository.save(csv.copy(state = startState))
        } else {
            csvRepository.save(csv)
        }
    }

    public override fun afterJob(jobExecution: JobExecution) {
        val id = extractCSVID(jobExecution)
        val csv = csvRepository.findById(id).orElseThrow { CSVNotFound(id) }

        check(!csv.state.isFinal) { """The state "${csv.state}" of CSV "${csv.id}" is final.""" }

        val targetState = when {
            jobExecution.status.isUnsuccessful -> failureState
            jobExecution.status == BatchStatus.STOPPING || jobExecution.status == BatchStatus.STOPPED -> stoppedState
            else -> successState
        }

        check(targetState isDirectlyAfter csv.state) { """Invalid progression of CSV state "${csv.state}" for CSV "${csv.id}". Expected one of ${csv.state.next.joinToString()}, found: $targetState.""" }

        if (targetState != csv.state) {
            csvRepository.save(csv.copy(state = targetState))
        }
    }
}
