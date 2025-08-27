package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.community.output.ContributorRepository
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.input.CSVUseCases
import org.orkg.dataimport.input.CreateCSVUseCase
import org.orkg.dataimport.input.ImportCSVUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.UpdateCSVUseCase
import org.orkg.dataimport.input.ValidateCSVUseCase
import org.orkg.dataimport.output.CSVRepository
import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobParametersBuilder
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
    private val typedCSVRecordRepository: TypedCSVRecordRepository,
    private val paperCSVRecordRepository: PaperCSVRecordRepository,
    private val paperCSVRecordImportResultRepository: PaperCSVRecordImportResultRepository,
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
            typedCSVRecordRepository.deleteAllByCSVID(csv.id)
            paperCSVRecordRepository.deleteAllByCSVID(csv.id)
            repository.save(
                updated.copy(
                    state = CSV.State.UPLOADED,
                    validationJobId = null,
                )
            )
        }
    }

    @Synchronized
    override fun validate(command: ValidateCSVUseCase.ValidateCommand): JobId {
        val csv = findByIdAndCreatedBy(command.id, command.contributorId)
            .orElseThrow { CSVNotFound(command.id) }
        if (csv.state isSameOrAfter State.VALIDATION_DONE || csv.state == State.VALIDATION_FAILED) {
            throw CSVAlreadyValidated(command.id)
        } else if (csv.state == State.VALIDATION_RUNNING || csv.state == State.VALIDATION_QUEUED) {
            throw CSVValidationAlreadyRunning(command.id)
        }
        val jobParameters = JobParametersBuilder()
            .add(CONTRIBUTOR_ID_FIELD, command.contributorId, false)
            .add(CSV_ID_FIELD, csv.id)
            .add(CSV_TYPE_FIELD, csv.type, false)
            .toJobParameters()
        try {
            repository.save(csv.copy(state = State.VALIDATION_QUEUED))
            return jobUseCases.runJob(csv.type.validateJobName, jobParameters)
        } catch (_: JobAlreadyRunning) {
            // align csv state with job state
            repository.save(csv.copy(state = State.VALIDATION_RUNNING))
            throw CSVValidationAlreadyRunning(command.id)
        } catch (e: JobRestartFailed) {
            // align csv state with job state
            repository.save(csv.copy(state = State.VALIDATION_FAILED))
            throw CSVValidationRestartFailed(command.id, e)
        } catch (_: JobAlreadyComplete) {
            // align csv state with job state
            repository.save(csv.copy(state = State.VALIDATION_DONE))
            throw CSVAlreadyValidated(command.id)
        }
    }

    @Synchronized
    override fun import(command: ImportCSVUseCase.ImportCommand): JobId {
        val csv = findByIdAndCreatedBy(command.id, command.contributorId)
            .orElseThrow { CSVNotFound(command.id) }
        if (csv.state == State.IMPORT_DONE || csv.state == State.IMPORT_FAILED) {
            throw CSVAlreadyImported(command.id)
        } else if (csv.state == State.IMPORT_RUNNING || csv.state == State.IMPORT_QUEUED) {
            throw CSVImportAlreadyRunning(command.id)
        } else if (csv.state isBefore State.VALIDATION_DONE || csv.state == State.VALIDATION_FAILED) {
            throw CSVNotValidated(command.id)
        }
        val jobParameters = JobParametersBuilder()
            .add(CONTRIBUTOR_ID_FIELD, command.contributorId, false)
            .add(CSV_ID_FIELD, csv.id)
            .toJobParameters()
        try {
            repository.save(csv.copy(state = State.IMPORT_QUEUED))
            return jobUseCases.runJob(csv.type.importJobName, jobParameters)
        } catch (_: JobAlreadyRunning) {
            // align csv state with job state
            repository.save(csv.copy(state = State.IMPORT_RUNNING))
            throw CSVImportAlreadyRunning(command.id)
        } catch (e: JobRestartFailed) {
            // align csv state with job state
            repository.save(csv.copy(state = State.IMPORT_FAILED))
            throw CSVImportRestartFailed(command.id, e)
        } catch (_: JobAlreadyComplete) {
            // align csv state with job state
            repository.save(csv.copy(state = State.IMPORT_DONE))
            throw CSVAlreadyImported(command.id)
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
        typedCSVRecordRepository.deleteAllByCSVID(csv.id)
        paperCSVRecordRepository.deleteAllByCSVID(csv.id)
        paperCSVRecordImportResultRepository.deleteAllByCSVID(csv.id)
    }
}
