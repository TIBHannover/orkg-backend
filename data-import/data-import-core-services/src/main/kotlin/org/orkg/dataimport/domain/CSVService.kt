package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.community.output.ContributorRepository
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.input.CSVUseCases
import org.orkg.dataimport.input.CreateCSVUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.UpdateCSVUseCase
import org.orkg.dataimport.output.CSVRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@Component
class CSVService(
    private val repository: CSVRepository,
    private val jobUseCases: JobUseCases,
    private val contributorRepository: ContributorRepository,
    private val clock: Clock,
) : CSVUseCases {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findByIdAndCreatedBy(id: CSVID, createdBy: ContributorId): Optional<CSV> =
        repository.findById(id).filter { it.createdBy == createdBy || contributorRepository.isAdmin(createdBy) }

    override fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<CSV> =
        repository.findAllByCreatedBy(createdBy, pageable)

    override fun create(command: CreateCSVUseCase.CreateCommand): CSVID {
        if (command.data.isBlank()) {
            throw CSVCannotBeBlank()
        }
        if (repository.existsByDataMD5(command.data.md5)) {
            throw CSVAlreadyExists()
        }
        val id = CSVID(UUID.randomUUID())
        val csv = CSV(
            id = id,
            name = command.name,
            type = command.type,
            format = command.format,
            state = State.UPLOADED,
            data = command.data,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
        )
        repository.save(csv)
        return id
    }

    override fun update(command: UpdateCSVUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val csv = findByIdAndCreatedBy(command.id, command.contributorId)
            .orElseThrow { CSVNotFound(command.id) }
        if (csv.state isAfter State.VALIDATION_DONE) {
            throw CSVAlreadyImported(csv.id)
        }
        if (command.data != null && command.data != csv.data && repository.existsByDataMD5(command.data!!.md5)) {
            throw CSVAlreadyExists()
        }
        val updated = csv.apply(command)
        if (updated != csv) {
            if (csv.validationJobId != null && (csv.state == State.VALIDATION_QUEUED || csv.state == State.VALIDATION_RUNNING)) {
                try {
                    jobUseCases.stopJob(csv.validationJobId!!, csv.createdBy)
                } catch (e: Throwable) {
                    logger.error("""Failed to stop job "{}".""", csv.validationJobId, e)
                }
            }
            repository.save(
                updated.copy(
                    state = CSV.State.UPLOADED,
                    validationJobId = null,
                )
            )
        }
    }

    override fun deleteById(id: CSVID, contributorId: ContributorId) {
        val csv = findByIdAndCreatedBy(id, contributorId).orElseThrow { CSVNotFound(id) }
        if ((csv.state == State.VALIDATION_QUEUED || csv.state == State.VALIDATION_RUNNING) && csv.validationJobId != null) {
            try {
                jobUseCases.stopJob(csv.validationJobId!!, contributorId)
            } catch (e: Throwable) {
                logger.error("""Failed to stop job "{}".""", csv.validationJobId, e)
            }
        }
        if ((csv.state == State.IMPORT_QUEUED || csv.state == State.IMPORT_RUNNING) && csv.importJobId != null) {
            try {
                jobUseCases.stopJob(csv.importJobId!!, contributorId)
            } catch (e: Throwable) {
                logger.error("""Failed to stop job "{}".""", csv.importJobId, e)
            }
        }
        repository.deleteById(id)
    }
}
