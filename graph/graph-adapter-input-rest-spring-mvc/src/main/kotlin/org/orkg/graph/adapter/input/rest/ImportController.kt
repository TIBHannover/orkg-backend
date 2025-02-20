package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.graph.input.ImportUseCases
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/import", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
class ImportController(
    private val service: ImportUseCases,
) {
    @RequireLogin
    @PostMapping("/resources")
    fun importResource(
        @RequestBody request: ImportRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ListRepresentation> {
        val id = when (request) {
            is ImportByURIRequest -> service.importResourceByURI(
                contributorId = currentUser.contributorId(),
                ontologyId = request.ontology,
                uri = request.uri
            )
            is ImportByShortFormRequest -> service.importResourceByShortForm(
                contributorId = currentUser.contributorId(),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
        val location = uriComponentsBuilder.path("/api/resources/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PostMapping("/predicates")
    fun importPredicate(
        @RequestBody request: ImportRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ListRepresentation> {
        val id = when (request) {
            is ImportByURIRequest -> service.importPredicateByURI(
                contributorId = currentUser.contributorId(),
                ontologyId = request.ontology,
                uri = request.uri
            )
            is ImportByShortFormRequest -> service.importPredicateByShortForm(
                contributorId = currentUser.contributorId(),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
        val location = uriComponentsBuilder.path("/api/predicates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PostMapping("/classes")
    fun importClass(
        @RequestBody request: ImportRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ListRepresentation> {
        val id = when (request) {
            is ImportByURIRequest -> service.importClassByURI(
                contributorId = currentUser.contributorId(),
                ontologyId = request.ontology,
                uri = request.uri
            )
            is ImportByShortFormRequest -> service.importClassByShortForm(
                contributorId = currentUser.contributorId(),
                ontologyId = request.ontology,
                shortForm = request.shortForm
            )
        }
        val location = uriComponentsBuilder.path("/api/classes/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(
        value = [
            JsonSubTypes.Type(ImportByURIRequest::class),
            JsonSubTypes.Type(ImportByShortFormRequest::class)
        ]
    )
    sealed interface ImportRequest

    data class ImportByURIRequest(
        val uri: ParsedIRI,
        val ontology: String,
    ) : ImportRequest

    data class ImportByShortFormRequest(
        @JsonProperty("short_form")
        val shortForm: String,
        val ontology: String,
    ) : ImportRequest
}
