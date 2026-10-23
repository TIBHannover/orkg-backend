package org.orkg.dataimport.domain.jobs

import org.orkg.dataimport.domain.RdfFileNotFound
import org.orkg.dataimport.domain.extractRdfFileId
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.output.RdfFileRepository
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener

class RdfFileStateUpdater(
    private val rdfFileRepository: RdfFileRepository,
    private val startState: State,
    private val successState: State,
    private val stoppedState: State,
    private val failureState: State,
    private val jobIdSetter: (RdfFile, JobId) -> RdfFile,
) : JobExecutionListener {
    init {
        require(successState isAfter startState) { "successState must be after startState" }
        require(stoppedState isAfter startState) { "stoppedState must be after startState" }
        require(failureState isAfter startState) { "failureState must be after startState" }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        val id = extractRdfFileId(jobExecution)
        val jobId = JobId(jobExecution.id)
        val rdf = rdfFileRepository.findById(id)
            .orElseThrow { RdfFileNotFound(id) }
            .let { jobIdSetter(it, jobId) }

        check(!rdf.state.isFinal) { """The state "${rdf.state}" of RDF "${rdf.id}" is final.""" }

        if (startState != rdf.state) {
            rdfFileRepository.save(rdf.copy(state = startState))
        } else {
            rdfFileRepository.save(rdf)
        }
    }

    override fun afterJob(jobExecution: JobExecution) {
        val id = extractRdfFileId(jobExecution)
        val rdf = rdfFileRepository.findById(id).orElseThrow { RdfFileNotFound(id) }

        check(!rdf.state.isFinal) { """The state "${rdf.state}" of RDF file "${rdf.id}" is final.""" }

        val targetState = when {
            jobExecution.status.isUnsuccessful -> failureState
            jobExecution.status == BatchStatus.STOPPING || jobExecution.status == BatchStatus.STOPPED -> stoppedState
            else -> successState
        }

        check(targetState isDirectlyAfter rdf.state) { """Invalid progression of RDF file state "${rdf.state}" for RDF file "${rdf.id}". Expected one of ${rdf.state.next.joinToString()}, found: $targetState.""" }

        if (targetState != rdf.state) {
            rdfFileRepository.save(rdf.copy(state = targetState))
        }
    }
}
