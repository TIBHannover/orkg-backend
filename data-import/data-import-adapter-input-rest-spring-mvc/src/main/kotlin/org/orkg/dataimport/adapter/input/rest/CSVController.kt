package org.orkg.dataimport.adapter.input.rest

import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.dataimport.adapter.input.rest.mapping.CSVRepresentationAdapter
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.input.CSVUseCases
import org.orkg.dataimport.input.CreateCSVUseCase
import org.orkg.dataimport.input.UpdateCSVUseCase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder

@RestController
@ConditionalOnProperty(value = ["orkg.import.csv.enabled"], havingValue = "true")
@RequestMapping("/api/csvs", produces = [MediaType.APPLICATION_JSON_VALUE])
class CSVController(
    private val csvUseCases: CSVUseCases,
) : CSVRepresentationAdapter {
    @RequireLogin
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: CSVID,
        currentUser: Authentication?,
    ): CSVRepresentation =
        csvUseCases.findByIdAndCreatedBy(id, currentUser.contributorId())
            .mapToCSVRepresentation()
            .orElseThrow { CSVNotFound(id) }

    @RequireLogin
    @GetMapping("/{id}/data", produces = ["text/csv"])
    fun findDataById(
        @PathVariable id: CSVID,
        currentUser: Authentication?,
    ): String =
        csvUseCases.findByIdAndCreatedBy(id, currentUser.contributorId())
            .map { it.data }
            .orElseThrow { CSVNotFound(id) }

    @RequireLogin
    @GetMapping
    fun findAll(
        pageable: Pageable,
        currentUser: Authentication?,
    ): Page<CSVRepresentation> =
        csvUseCases.findAllByCreatedBy(currentUser.contributorId(), pageable)
            .mapToCSVRepresentation()

    @RequireLogin
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun create(
        @ModelAttribute request: CreateCSVRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val id = csvUseCases.create(
            CreateCSVUseCase.CreateCommand(
                contributorId = contributorId,
                name = request.file.originalFilename ?: "${request.file.name}.csv",
                format = request.format,
                type = request.type,
                data = String(request.file.bytes)
            )
        )
        val location = uriComponentsBuilder
            .path("/api/csvs/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun update(
        @PathVariable id: CSVID,
        @ModelAttribute request: UpdateCSVRequest,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        csvUseCases.update(
            UpdateCSVUseCase.UpdateCommand(
                id = id,
                contributorId = currentUser.contributorId(),
                name = request.file?.let { it.originalFilename ?: "${it.name}.csv" },
                format = request.format,
                type = request.type,
                data = request.file?.let { String(it.bytes) }
            )
        )
        return noContent().build()
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable id: CSVID,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        csvUseCases.deleteById(id, currentUser.contributorId())
        return noContent().build()
    }

    data class CreateCSVRequest(
        val file: MultipartFile,
        val type: CSV.Type,
        val format: CSV.Format = CSV.Format.DEFAULT,
    )

    data class UpdateCSVRequest(
        val file: MultipartFile?,
        val type: CSV.Type?,
        val format: CSV.Format?,
    )
}
