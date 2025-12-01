package org.orkg.dataimport.adapter.input.rest

import org.orkg.common.annotations.RequireAdminRole
import org.orkg.common.contributorId
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationAdapter
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.adapter.input.rest.mapping.JobStatusRepresentationAdapter
import org.orkg.dataimport.domain.JobNotComplete
import org.orkg.dataimport.domain.JobNotFound
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.input.JobUseCases
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/jobs")
@ConditionalOnProperty(value = ["orkg.import.csv.enabled"], havingValue = "true")
class JobController(
    private val jobUseCases: JobUseCases,
    override val jobResultRepresentationFactory: JobResultRepresentationFactory,
) : JobStatusRepresentationAdapter,
    JobResultRepresentationAdapter {
    @RequireAdminRole
    @GetMapping("/{id}")
    fun findStatusById(
        @PathVariable id: JobId,
        currentUser: Authentication?,
    ): JobStatusRepresentation =
        jobUseCases.findJobStatusById(id, currentUser.contributorId())
            .mapToJobStatusRepresentation()
            .orElseThrow { JobNotFound(id) }

    @RequireAdminRole
    @GetMapping("/{id}/results")
    fun findResultsById(
        @PathVariable id: JobId,
        pageable: Pageable,
        currentUser: Authentication?,
    ): ResponseEntity<Any> =
        jobUseCases.findJobResultById(id, currentUser.contributorId(), pageable)
            .mapToJobResultRepresentation()
            .orElseThrow { JobNotComplete(id) }

    @RequireAdminRole
    @DeleteMapping("/{id}")
    fun stopById(
        @PathVariable id: JobId,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        jobUseCases.stopJob(id, currentUser.contributorId())
        return noContent().build()
    }
}
