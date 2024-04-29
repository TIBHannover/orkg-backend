package org.orkg.community.adapter.input.rest

import java.time.Duration
import org.orkg.common.ContributorId
import org.orkg.common.withCacheControl
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.graph.domain.ContributorNotFound
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributors", produces = [MediaType.APPLICATION_JSON_VALUE])
class ContributorController(
    val retrieveContributor: RetrieveContributorUseCase
) {
    @GetMapping("/{id}")
    fun getContributorById(@PathVariable id: ContributorId): ResponseEntity<Contributor> =
        retrieveContributor.findById(id).orElseThrow { ContributorNotFound(id) }
            .withCacheControl(Duration.ofMinutes(5))
}
