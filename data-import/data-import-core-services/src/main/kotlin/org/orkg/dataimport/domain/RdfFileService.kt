package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.community.output.ContributorRepository
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.input.CreateRdfFileUseCase
import org.orkg.dataimport.input.ImportRdfFileUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.RdfFileUseCases
import org.orkg.dataimport.input.UpdateRdfFileUseCase
import org.orkg.dataimport.input.ValidateRdfFileUseCase
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@Component
class RdfFileService(
    private val repository: RdfFileRepository,
    private val jobUseCases: JobUseCases,
    private val contributorRepository: ContributorRepository,
    private val rdfClassRepository: RdfClassRepository,
    private val rdfPredicateRepository: RdfPredicateRepository,
    private val rdfResourceRepository: RdfResourceRepository,
    private val rdfTripleRepository: RdfTripleRepository,
    private val clock: Clock,
) : RdfFileUseCases {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findByIdAndCreatedBy(id: UUID, createdBy: ContributorId): Optional<RdfFile> =
        repository.findById(id).filter { it.createdBy == createdBy || contributorRepository.isAdmin(createdBy) }

    override fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<RdfFile> =
        repository.findAllByCreatedBy(createdBy, pageable)

    override fun create(command: CreateRdfFileUseCase.CreateCommand): UUID {
        if (command.data.isBlank()) {
            throw RdfFileCannotBeBlank()
        }
        if (repository.existsByDataMD5(command.data.md5)) {
            throw RdfFileAlreadyExists()
        }
        val id = UUID.randomUUID()
        val rdfFile = RdfFile(
            id = id,
            name = command.name,
            baseUri = command.baseUri,
            format = command.format,
            state = State.UPLOADED,
            data = command.data,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
        )
        repository.save(rdfFile)
        return id
    }

    override fun update(command: UpdateRdfFileUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val rdfFile = findByIdAndCreatedBy(command.id, command.contributorId)
            .orElseThrow { RdfFileNotFound(command.id) }
        if (rdfFile.state isAfter State.VALIDATION_DONE) {
            throw RdfFileAlreadyImported(rdfFile.id)
        }
        if (command.data != null && command.data != rdfFile.data && repository.existsByDataMD5(command.data!!.md5)) {
            throw RdfFileAlreadyExists()
        }
        val updated = rdfFile.apply(command)
        if (updated != rdfFile) {
            if (rdfFile.validationJobId != null && (rdfFile.state == State.VALIDATION_QUEUED || rdfFile.state == State.VALIDATION_RUNNING)) {
                try {
                    jobUseCases.stopJob(rdfFile.validationJobId!!, rdfFile.createdBy)
                } catch (e: Throwable) {
                    logger.error("""Failed to stop job "{}".""", rdfFile.validationJobId, e)
                }
            }
            rdfClassRepository.deleteAllByRdfFileId(rdfFile.id)
            rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id)
            rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id)
            rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id)
            repository.save(
                updated.copy(
                    state = State.UPLOADED,
                    validationJobId = null,
                ),
            )
        }
    }

    @Synchronized
    override fun validate(command: ValidateRdfFileUseCase.ValidateCommand): JobId {
        val rdfFile = findByIdAndCreatedBy(command.id, command.contributorId)
            .orElseThrow { RdfFileNotFound(command.id) }
        if (rdfFile.state isSameOrAfter State.VALIDATION_DONE || rdfFile.state == State.VALIDATION_FAILED) {
            throw RdfFileAlreadyValidated(command.id)
        } else if (rdfFile.state == State.VALIDATION_RUNNING || rdfFile.state == State.VALIDATION_QUEUED) {
            throw RdfFileValidationAlreadyRunning(command.id)
        }
        val jobParameters = JobParametersBuilder()
            .add(CONTRIBUTOR_ID_FIELD, command.contributorId, false)
            .add(RDF_FILE_ID_FIELD, rdfFile.id)
            .toJobParameters()
        try {
            repository.save(rdfFile.copy(state = State.VALIDATION_QUEUED))
            return jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, jobParameters, true)
        } catch (_: JobAlreadyRunning) {
            // align rdf file state with job state
            repository.save(rdfFile.copy(state = State.VALIDATION_RUNNING))
            throw RdfFileValidationAlreadyRunning(command.id)
        } catch (e: JobRestartFailed) {
            // align rdf file state with job state
            repository.save(rdfFile.copy(state = State.VALIDATION_FAILED))
            throw RdfFileValidationRestartFailed(command.id, e)
        } catch (_: JobAlreadyComplete) {
            // align rdf file state with job state
            repository.save(rdfFile.copy(state = State.VALIDATION_DONE))
            throw RdfFileAlreadyValidated(command.id)
        }
    }

    @Synchronized
    override fun import(command: ImportRdfFileUseCase.ImportCommand): JobId {
        val rdfFile = findByIdAndCreatedBy(command.id, command.contributorId)
            .orElseThrow { RdfFileNotFound(command.id) }
        if (rdfFile.state == State.IMPORT_DONE || rdfFile.state == State.IMPORT_FAILED) {
            throw RdfFileAlreadyImported(command.id)
        } else if (rdfFile.state == State.IMPORT_RUNNING || rdfFile.state == State.IMPORT_QUEUED) {
            throw RdfFileImportAlreadyRunning(command.id)
        } else if (rdfFile.state isBefore State.VALIDATION_DONE || rdfFile.state == State.VALIDATION_FAILED) {
            throw RdfFileNotValidated(command.id)
        }
        val jobParameters = JobParametersBuilder()
            .add(CONTRIBUTOR_ID_FIELD, command.contributorId, false)
            .add(RDF_FILE_ID_FIELD, rdfFile.id)
            .add(EXTRACTION_METHOD_FIELD, command.extractionMethod)
            .add(MODIFIABLE_FIELD, command.modifiable)
            .toJobParameters()
        try {
            repository.save(rdfFile.copy(state = State.IMPORT_QUEUED))
            return jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, jobParameters)
        } catch (_: JobAlreadyRunning) {
            // align rdf file state with job state
            repository.save(rdfFile.copy(state = State.IMPORT_RUNNING))
            throw RdfFileImportAlreadyRunning(command.id)
        } catch (e: JobRestartFailed) {
            // align rdf file state with job state
            repository.save(rdfFile.copy(state = State.IMPORT_FAILED))
            throw RdfFileImportRestartFailed(command.id, e)
        } catch (_: JobAlreadyComplete) {
            // align rdf file state with job state
            repository.save(rdfFile.copy(state = State.IMPORT_DONE))
            throw RdfFileAlreadyImported(command.id)
        }
    }

    override fun deleteById(id: UUID, contributorId: ContributorId) {
        val rdfFile = findByIdAndCreatedBy(id, contributorId).orElseThrow { RdfFileNotFound(id) }
        if ((rdfFile.state == State.VALIDATION_QUEUED || rdfFile.state == State.VALIDATION_RUNNING) && rdfFile.validationJobId != null) {
            try {
                jobUseCases.stopJob(rdfFile.validationJobId!!, contributorId)
            } catch (e: Throwable) {
                logger.error("""Failed to stop job "{}".""", rdfFile.validationJobId, e)
            }
        }
        if ((rdfFile.state == State.IMPORT_QUEUED || rdfFile.state == State.IMPORT_RUNNING) && rdfFile.importJobId != null) {
            try {
                jobUseCases.stopJob(rdfFile.importJobId!!, contributorId)
            } catch (e: Throwable) {
                logger.error("""Failed to stop job "{}".""", rdfFile.importJobId, e)
            }
        }
        repository.deleteById(id)
        rdfClassRepository.deleteAllByRdfFileId(rdfFile.id)
        rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id)
        rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id)
        rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id)
    }

    override fun deleteAll() {
        repository.deleteAll()
        rdfClassRepository.deleteAll()
        rdfPredicateRepository.deleteAll()
        rdfResourceRepository.deleteAll()
        rdfTripleRepository.deleteAll()
    }
}
