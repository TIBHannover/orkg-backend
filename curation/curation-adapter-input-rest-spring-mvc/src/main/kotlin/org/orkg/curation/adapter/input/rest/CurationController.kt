package org.orkg.curation.adapter.input.rest

import org.orkg.curation.input.RetrieveCurationUseCase
import org.orkg.graph.adapter.input.rest.PredicateRepresentation
import org.orkg.graph.adapter.input.rest.mapping.PredicateRepresentationAdapter
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/curation", produces = [MediaType.APPLICATION_JSON_VALUE])
class CurationController(
    private val service: RetrieveCurationUseCase,
    override val statementService: StatementUseCases
) : PredicateRepresentationAdapter {

    @GetMapping("/predicates-without-descriptions")
    fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<PredicateRepresentation> =
        service.findAllPredicatesWithoutDescriptions(pageable)
            .map { it.toPredicateRepresentation(null) }
}
