package org.orkg.dataimport.input

import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface CSVUseCases :
    RetrieveCSVUseCase,
    CreateCSVUseCase,
    UpdateCSVUseCase,
    ValidateCSVUseCase,
    ImportCSVUseCase,
    DeleteCSVUseCase

interface RetrieveCSVUseCase {
    fun findByIdAndCreatedBy(id: CSVID, createdBy: ContributorId): Optional<CSV>

    fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<CSV>
}

interface CreateCSVUseCase {
    fun create(command: CreateCommand): CSVID

    data class CreateCommand(
        val contributorId: ContributorId,
        val name: String,
        val data: String,
        val type: CSV.Type,
        val format: CSV.Format,
    )
}

interface UpdateCSVUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: CSVID,
        val contributorId: ContributorId,
        val name: String? = null,
        val data: String? = null,
        val type: CSV.Type? = null,
        val format: CSV.Format? = null,
    )
}

interface ValidateCSVUseCase {
    fun validate(command: ValidateCommand): JobId

    data class ValidateCommand(
        val id: CSVID,
        val contributorId: ContributorId,
    )
}

interface ImportCSVUseCase {
    fun import(command: ImportCommand): JobId

    data class ImportCommand(
        val id: CSVID,
        val contributorId: ContributorId,
    )
}

interface DeleteCSVUseCase {
    fun deleteById(id: CSVID, contributorId: ContributorId)
}
