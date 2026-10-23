package org.orkg.dataimport.adapter.input.rest

import org.orkg.common.IRI
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationAdapter
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.adapter.input.rest.mapping.JobStatusRepresentationAdapter
import org.orkg.dataimport.adapter.input.rest.mapping.RdfFileRepresentationAdapter
import org.orkg.dataimport.domain.RdfFileImportJobNotFound
import org.orkg.dataimport.domain.RdfFileNotFound
import org.orkg.dataimport.domain.RdfFileValidationJobNotFound
import org.orkg.dataimport.domain.UnsupportedRdfFormat
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.input.CreateRdfFileUseCase
import org.orkg.dataimport.input.ImportRdfFileUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.RdfFileUseCases
import org.orkg.dataimport.input.UpdateRdfFileUseCase
import org.orkg.dataimport.input.ValidateRdfFileUseCase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.status
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.BindParam
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
import java.util.UUID

@RestController
@RequestMapping("/api/rdf-files")
@ConditionalOnProperty(value = ["orkg.import.rdf.enabled"], havingValue = "true")
class RdfFileController(
    private val rdfFileUseCases: RdfFileUseCases,
    private val jobUseCases: JobUseCases,
    override val jobResultRepresentationFactory: JobResultRepresentationFactory,
) : RdfFileRepresentationAdapter,
    JobStatusRepresentationAdapter,
    JobResultRepresentationAdapter {
    @RequireLogin
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findById(
        @PathVariable id: UUID,
        currentUser: Authentication?,
    ): RdfFileRepresentation =
        rdfFileUseCases.findByIdAndCreatedBy(id, currentUser.contributorId())
            .mapToRdfFileRepresentation()
            .orElseThrow { RdfFileNotFound(id) }

    @RequireLogin
    @GetMapping("/{id}/data")
    fun findDataById(
        @PathVariable id: UUID,
        currentUser: Authentication?,
    ): ResponseEntity<String> =
        rdfFileUseCases.findByIdAndCreatedBy(id, currentUser.contributorId())
            .map { rdfFile ->
                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(rdfFile.format.contentType))
                    .body(rdfFile.data)
            }
            .orElseThrow { RdfFileNotFound(id) }

    @RequireLogin
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAll(
        pageable: Pageable,
        currentUser: Authentication?,
    ): Page<RdfFileRepresentation> =
        rdfFileUseCases.findAllByCreatedBy(currentUser.contributorId(), pageable)
            .mapToRdfFileRepresentation()

    @RequireLogin
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @ModelAttribute request: CreateRdfFileRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val format = RdfFile.Format.fromContentType(request.file.contentType)
            ?: throw UnsupportedRdfFormat(request.file.contentType)
        val id = rdfFileUseCases.create(
            CreateRdfFileUseCase.CreateCommand(
                contributorId = contributorId,
                name = request.file.originalFilename ?: "${request.file.name}.${format.fileExtension}",
                format = format,
                baseUri = request.baseUri,
                data = String(request.file.bytes),
            ),
        )
        val location = uriComponentsBuilder
            .path("/api/rdf-files/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: UUID,
        @ModelAttribute request: UpdateRdfFileRequest,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val format = request.file?.let { file ->
            RdfFile.Format.fromContentType(file.contentType) ?: throw UnsupportedRdfFormat(file.contentType)
        }
        rdfFileUseCases.update(
            UpdateRdfFileUseCase.UpdateCommand(
                id = id,
                contributorId = currentUser.contributorId(),
                name = request.file?.let { file -> file.originalFilename ?: "${file.name}${format!!.fileExtension}" },
                format = format,
                baseUri = request.baseUri,
                data = request.file?.let { String(it.bytes) },
            ),
        )
        return noContent().build()
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable id: UUID,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        rdfFileUseCases.deleteById(id, currentUser.contributorId())
        return noContent().build()
    }

    @RequireLogin
    @GetMapping("/{id}/validate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findValidationStatusById(
        @PathVariable("id") id: UUID,
        currentUser: Authentication?,
    ): JobStatusRepresentation {
        val contributorId = currentUser.contributorId()
        val rdfFile = rdfFileUseCases.findByIdAndCreatedBy(id, contributorId)
            .orElseThrow { RdfFileNotFound(id) }
        if (rdfFile.validationJobId == null) {
            throw RdfFileValidationJobNotFound(id)
        }
        return jobUseCases.findJobStatusById(rdfFile.validationJobId!!, contributorId)
            .mapToJobStatusRepresentation()
            .orElseThrow { RdfFileValidationJobNotFound(id, rdfFile.validationJobId) }
    }

    @RequireLogin
    @GetMapping("/{id}/validate/results", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findValidationResultsById(
        @PathVariable("id") id: UUID,
        pageable: Pageable,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val rdfFile = rdfFileUseCases.findByIdAndCreatedBy(id, contributorId)
            .orElseThrow { RdfFileNotFound(id) }
        if (rdfFile.validationJobId == null) {
            throw RdfFileValidationJobNotFound(id)
        }
        return jobUseCases.findJobResultById(rdfFile.validationJobId!!, contributorId, pageable)
            .mapToJobResultRepresentation()
            .orElseThrow { RdfFileValidationJobNotFound(id, rdfFile.validationJobId) }
    }

    @RequireLogin
    @PostMapping("/{id}/validate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startValidationById(
        @PathVariable("id") id: UUID,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        rdfFileUseCases.validate(ValidateRdfFileUseCase.ValidateCommand(id, contributorId))
        return status(ACCEPTED).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}/validate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun stopValidationById(
        @PathVariable("id") id: UUID,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val rdfFile = rdfFileUseCases.findByIdAndCreatedBy(id, contributorId)
            .orElseThrow { RdfFileNotFound(id) }
        if (rdfFile.validationJobId == null) {
            throw RdfFileValidationJobNotFound(id)
        }
        jobUseCases.stopJob(rdfFile.validationJobId!!, contributorId)
        return noContent().build()
    }

    @RequireLogin
    @GetMapping("/{id}/import", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findImportStatusById(
        @PathVariable("id") id: UUID,
        currentUser: Authentication?,
    ): JobStatusRepresentation {
        val contributorId = currentUser.contributorId()
        val rdfFile = rdfFileUseCases.findByIdAndCreatedBy(id, contributorId)
            .orElseThrow { RdfFileNotFound(id) }
        if (rdfFile.importJobId == null) {
            throw RdfFileImportJobNotFound(id)
        }
        return jobUseCases.findJobStatusById(rdfFile.importJobId!!, contributorId)
            .mapToJobStatusRepresentation()
            .orElseThrow { RdfFileImportJobNotFound(id, rdfFile.validationJobId) }
    }

    @RequireLogin
    @GetMapping("/{id}/import/results", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findImportResultsById(
        @PathVariable("id") id: UUID,
        pageable: Pageable,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val rdfFile = rdfFileUseCases.findByIdAndCreatedBy(id, contributorId)
            .orElseThrow { RdfFileNotFound(id) }
        if (rdfFile.importJobId == null) {
            throw RdfFileImportJobNotFound(id)
        }
        return jobUseCases.findJobResultById(rdfFile.importJobId!!, contributorId, pageable)
            .mapToJobResultRepresentation()
            .orElseThrow { RdfFileImportJobNotFound(id, rdfFile.validationJobId) }
    }

    @RequireLogin
    @PostMapping("/{id}/import", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startImportById(
        @PathVariable id: UUID,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        rdfFileUseCases.import(ImportRdfFileUseCase.ImportCommand(id, contributorId))
        return status(ACCEPTED).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}/import", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun stopImportById(
        @PathVariable("id") id: UUID,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val rdfFile = rdfFileUseCases.findByIdAndCreatedBy(id, contributorId)
            .orElseThrow { RdfFileNotFound(id) }
        if (rdfFile.importJobId == null) {
            throw RdfFileImportJobNotFound(id)
        }
        jobUseCases.stopJob(rdfFile.importJobId!!, contributorId)
        return noContent().build()
    }

    data class CreateRdfFileRequest(
        val file: MultipartFile,
        @field:BindParam("base_uri")
        val baseUri: IRI?,
    )

    data class UpdateRdfFileRequest(
        val file: MultipartFile?,
        val format: RdfFile.Format?,
        @field:BindParam("base_uri")
        val baseUri: IRI?,
    )
}
