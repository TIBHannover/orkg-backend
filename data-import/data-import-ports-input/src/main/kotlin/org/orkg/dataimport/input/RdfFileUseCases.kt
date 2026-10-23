package org.orkg.dataimport.input

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.graph.domain.ExtractionMethod
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface RdfFileUseCases :
    RetrieveRdfFileUseCase,
    CreateRdfFileUseCase,
    UpdateRdfFileUseCase,
    ValidateRdfFileUseCase,
    ImportRdfFileUseCase,
    DeleteRdfFileUseCase

interface RetrieveRdfFileUseCase {
    fun findByIdAndCreatedBy(id: UUID, createdBy: ContributorId): Optional<RdfFile>

    fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<RdfFile>
}

interface CreateRdfFileUseCase {
    fun create(command: CreateCommand): UUID

    data class CreateCommand(
        val contributorId: ContributorId,
        val name: String,
        val data: String,
        val baseUri: IRI?,
        val format: RdfFile.Format,
    )
}

interface UpdateRdfFileUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: UUID,
        val contributorId: ContributorId,
        val name: String? = null,
        val data: String? = null,
        val baseUri: IRI? = null,
        val format: RdfFile.Format? = null,
    )
}

interface ValidateRdfFileUseCase {
    fun validate(command: ValidateCommand): JobId

    data class ValidateCommand(
        val id: UUID,
        val contributorId: ContributorId,
    )
}

interface ImportRdfFileUseCase {
    fun import(command: ImportCommand): JobId

    data class ImportCommand(
        val id: UUID,
        val contributorId: ContributorId,
        val extractionMethod: ExtractionMethod = ExtractionMethod.AUTOMATIC,
        val modifiable: Boolean = true,
    )
}

interface DeleteRdfFileUseCase {
    fun deleteById(id: UUID, contributorId: ContributorId)

    fun deleteAll()
}
