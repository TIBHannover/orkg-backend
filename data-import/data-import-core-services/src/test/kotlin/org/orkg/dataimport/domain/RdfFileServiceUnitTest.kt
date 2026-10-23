package org.orkg.dataimport.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.md5
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.rdf.RdfFile.Format
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.domain.testing.fixtures.createRdfFile
import org.orkg.dataimport.input.ImportRdfFileUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.UpdateRdfFileUseCase
import org.orkg.dataimport.input.ValidateRdfFileUseCase
import org.orkg.dataimport.input.testing.fixtures.createRdfFileCommand
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.testing.MockUserId
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class RdfFileServiceUnitTest : MockkBaseTest {
    private val repository: RdfFileRepository = mockk()
    private val jobUseCases: JobUseCases = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val rdfClassRepository: RdfClassRepository = mockk()
    private val rdfPredicateRepository: RdfPredicateRepository = mockk()
    private val rdfResourceRepository: RdfResourceRepository = mockk()
    private val rdfTripleRepository: RdfTripleRepository = mockk()

    private val service = RdfFileService(
        repository,
        jobUseCases,
        contributorRepository,
        rdfClassRepository,
        rdfPredicateRepository,
        rdfResourceRepository,
        rdfTripleRepository,
        fixedClock,
    )

    @Test
    fun `Given an rdf file id and a contributor id, when fetching the rdfFile, and the user is the owner, it returns the rdfFile`() {
        val rdfFile = createRdfFile()

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        service.findByIdAndCreatedBy(rdfFile.id, rdfFile.createdBy) shouldBe Optional.of(rdfFile)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file id and a contributor id, when fetching the rdfFile, and the user is an admin, it returns the rdfFile`() {
        val rdfFile = createRdfFile()
        val admin = createContributor(isAdmin = true)

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)

        service.findByIdAndCreatedBy(rdfFile.id, admin.id) shouldBe Optional.of(rdfFile)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
    }

    @Test
    fun `Given an rdf file id and a contributor id, when fetching the rdfFile, and the user is not the owner and not an admin, it returns an empty result`() {
        val rdfFile = createRdfFile()
        val otherUser = createContributor()

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        service.findByIdAndCreatedBy(rdfFile.id, otherUser.id).isEmpty shouldBe true

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given an rdf file create command, when inputs are valid, it creates a new rdfFile`() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val command = createRdfFileCommand()

        mockkStatic(UUID::class) {
            every { repository.existsByDataMD5(command.data.md5) } returns false
            every { UUID.randomUUID() } returns id
            every { repository.save(any()) } just runs

            service.create(command) shouldBe id

            verify(exactly = 1) { repository.existsByDataMD5(command.data.md5) }
            verify(exactly = 1) { UUID.randomUUID() }
            verify(exactly = 1) {
                repository.save(
                    withArg {
                        it.id shouldBe id
                        it.name shouldBe command.name
                        it.baseUri shouldBe command.baseUri
                        it.format shouldBe command.format
                        it.state shouldBe State.UPLOADED
                        it.data shouldBe command.data
                        it.createdBy shouldBe command.contributorId
                        it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    },
                )
            }
        }
    }

    @Test
    fun `Given an rdf file create command, when data is blank, it throws an exception`() {
        val command = createRdfFileCommand().copy(data = "  ")

        shouldThrow<RdfFileCannotBeBlank> { service.create(command) }
    }

    @Test
    fun `Given an rdf file create command, when another rdfFile with the same data already exists, it throws an exception`() {
        val command = createRdfFileCommand()

        every { repository.existsByDataMD5(command.data.md5) } returns true

        shouldThrow<RdfFileAlreadyExists> { service.create(command) }

        verify(exactly = 1) { repository.existsByDataMD5(command.data.md5) }
    }

    @Test
    fun `Given an rdf file update command, when updating all properties, and the user is the owner, it returns success`() {
        val rdfFile = createRdfFile()
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
            name = "changed",
            data = "changed",
            baseUri = IRI.create("https://orkg.org"),
            format = Format.N3,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.existsByDataMD5(command.data!!.md5) } returns false
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.existsByDataMD5(command.data!!.md5) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe rdfFile.id
                    it.name shouldBe command.name
                    it.baseUri shouldBe command.baseUri
                    it.format shouldBe command.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe command.data
                    it.createdBy shouldBe rdfFile.createdBy
                    it.createdAt shouldBe rdfFile.createdAt
                },
            )
        }
    }

    @Test
    fun `Given an rdf file update command, when updating all properties, and the user is an admin, it returns success`() {
        val rdfFile = createRdfFile()
        val admin = createContributor(isAdmin = true)
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = admin.id,
            name = "changed",
            data = "changed",
            baseUri = IRI.create("https://orkg.org"),
            format = Format.N3,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { repository.existsByDataMD5(command.data!!.md5) } returns false
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { repository.existsByDataMD5(command.data!!.md5) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe rdfFile.id
                    it.name shouldBe command.name
                    it.baseUri shouldBe command.baseUri
                    it.format shouldBe command.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe command.data
                    it.createdBy shouldBe rdfFile.createdBy
                    it.createdAt shouldBe rdfFile.createdAt
                },
            )
        }
    }

    @Test
    fun `Given an rdf file update command, when updating all properties, and the user is not the owner and not an admin, it throws an exception`() {
        val rdfFile = createRdfFile().copy(
            state = State.VALIDATION_DONE,
            validationJobId = JobId(123),
        )
        val otherUser = createContributor(isAdmin = false)
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = otherUser.id,
            data = "changed",
            baseUri = IRI.create("https://orkg.org"),
            format = Format.N3,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<RdfFileNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given an rdf file update command, when updating no properties, it does nothing`() {
        val rdfFile = createRdfFile()
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        service.update(command)
    }

    @Test
    fun `Given an rdf file update command, when validation is currently running, it stops the job`() {
        val rdfFile = createRdfFile().copy(
            state = State.VALIDATION_RUNNING,
            validationJobId = JobId(123),
        )
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
            name = "changed",
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { jobUseCases.stopJob(rdfFile.validationJobId!!, rdfFile.createdBy) } just runs
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { jobUseCases.stopJob(rdfFile.validationJobId!!, rdfFile.createdBy) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe rdfFile.id
                    it.name shouldBe command.name
                    it.baseUri shouldBe command.baseUri
                    it.format shouldBe rdfFile.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe rdfFile.data
                    it.createdBy shouldBe rdfFile.createdBy
                    it.createdAt shouldBe rdfFile.createdAt
                },
            )
        }
    }

    @Test
    fun `Given an rdf file update command, when rdfFile does not exist, it throws an exception`() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            data = "changed",
        )

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<RdfFileNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @ParameterizedTest
    @EnumSource(State::class, names = ["IMPORT_RUNNING", "IMPORT_STOPPED", "IMPORT_FAILED", "IMPORT_DONE"])
    fun `Given an rdf file update command, when rdfFile is already imported, it throws an exception`(state: State) {
        val rdfFile = createRdfFile().copy(state = state)
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
            data = "changed",
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<RdfFileAlreadyImported> { service.update(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file update command, when another rdfFile with the same data already exists, it throws an exception`() {
        val rdfFile = createRdfFile()
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
            data = "changed",
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.existsByDataMD5(command.data!!.md5) } returns true

        shouldThrow<RdfFileAlreadyExists> { service.update(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.existsByDataMD5(command.data!!.md5) }
    }

    @Test
    fun `Given an rdf file update command, when data is identical, it does not query the rdf file repository for duplicates`() {
        val rdfFile = createRdfFile()
        val command = UpdateRdfFileUseCase.UpdateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
            name = "changed",
            data = rdfFile.data,
            baseUri = IRI.create("https://orkg.org"),
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe rdfFile.id
                    it.name shouldBe command.name
                    it.baseUri shouldBe command.baseUri
                    it.format shouldBe rdfFile.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe rdfFile.data
                    it.createdBy shouldBe rdfFile.createdBy
                    it.createdAt shouldBe rdfFile.createdAt
                },
            )
        }
    }

    @Test
    fun `Given an rdf file validation command, when rdfFile state is valid, and the user is the owner, it starts the validation job`() {
        val rdfFile = createRdfFile()
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )
        val jobId = JobId(123)

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) } returns jobId

        service.validate(command) shouldBe jobId

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) }
    }

    @Test
    fun `Given an rdf file validation command, when rdfFile state is valid, and the user is an admin, it starts the validation job`() {
        val rdfFile = createRdfFile()
        val admin = createContributor(isAdmin = true)
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = admin.id,
        )
        val jobId = JobId(123)

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) } returns jobId

        service.validate(command) shouldBe jobId

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) }
    }

    @Test
    fun `Given an rdf file validation command, when rdfFile state is valid, and the user is not the owner and not an admin, it throws an exception`() {
        val rdfFile = createRdfFile()
        val otherUser = createContributor()
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = otherUser.id,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<RdfFileNotFound> { service.validate(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given an rdf file validation command, when rdfFile does not exist, it throws an exception`() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
        )

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<RdfFileNotFound> { service.validate(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @ParameterizedTest
    @EnumSource(
        State::class,
        names = ["VALIDATION_DONE", "VALIDATION_FAILED", "IMPORT_RUNNING", "IMPORT_STOPPED", "IMPORT_FAILED", "IMPORT_DONE"],
    )
    fun `Given an rdf file validation command, when rdfFile is already validated, it throws an exception`(state: State) {
        val rdfFile = createRdfFile().copy(state = state)
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<RdfFileAlreadyValidated> { service.validate(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @ParameterizedTest
    @EnumSource(
        State::class,
        names = ["VALIDATION_QUEUED", "VALIDATION_RUNNING"],
    )
    fun `Given an rdf file validation command, when rdfFile validation is already running, it throws an exception`(state: State) {
        val rdfFile = createRdfFile().copy(state = state)
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<RdfFileValidationAlreadyRunning> { service.validate(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file validation command, when job service reports job already running, it throws an exception`() {
        val rdfFile = createRdfFile()
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) } throws JobAlreadyRunning(JobId(123))

        shouldThrow<RdfFileValidationAlreadyRunning> { service.validate(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_RUNNING)) }
    }

    @Test
    fun `Given an rdf file validation command, when job service reports job restart failed, it throws an exception`() {
        val rdfFile = createRdfFile()
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) } throws JobRestartFailed(JobId(123), RuntimeException())

        shouldThrow<RdfFileValidationRestartFailed> { service.validate(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_FAILED)) }
    }

    @Test
    fun `Given an rdf file validation command, when job service reports job already complete, it throws an exception`() {
        val rdfFile = createRdfFile()
        val command = ValidateRdfFileUseCase.ValidateCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) } throws JobAlreadyComplete(JobId(123))

        shouldThrow<RdfFileAlreadyValidated> { service.validate(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.VALIDATE_RDF_FILE, any(), any()) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.VALIDATION_DONE)) }
    }

    @Test
    fun `Given an rdf file import command, when rdfFile state is valid, and the user is the owner, it starts the import job`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_DONE)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )
        val jobId = JobId(123)

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) } returns jobId

        service.import(command) shouldBe jobId

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) }
    }

    @Test
    fun `Given an rdf file import command, when rdfFile state is valid, and the user is an admin, it starts the import job`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_DONE)
        val admin = createContributor(isAdmin = true)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = admin.id,
        )
        val jobId = JobId(123)

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) } returns jobId

        service.import(command) shouldBe jobId

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) }
    }

    @Test
    fun `Given an rdf file import command, when rdfFile state is valid, and the user is not the owner and not an admin, it throws an exception`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_DONE)
        val otherUser = createContributor()
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = otherUser.id,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<RdfFileNotFound> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given an rdf file import command, when rdfFile does not exist, it throws an exception`() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val command = ImportRdfFileUseCase.ImportCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
        )

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<RdfFileNotFound> { service.import(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @ParameterizedTest
    @EnumSource(
        State::class,
        names = ["IMPORT_FAILED", "IMPORT_DONE"],
    )
    fun `Given an rdf file import command, when rdfFile is already imported, it throws an exception`(state: State) {
        val rdfFile = createRdfFile().copy(state = state)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<RdfFileAlreadyImported> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @ParameterizedTest
    @EnumSource(
        State::class,
        names = ["IMPORT_QUEUED", "IMPORT_RUNNING"],
    )
    fun `Given an rdf file import command, when rdfFile import is already running, it throws an exception`(state: State) {
        val rdfFile = createRdfFile().copy(state = state)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<RdfFileImportAlreadyRunning> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @ParameterizedTest
    @EnumSource(
        State::class,
        names = ["UPLOADED", "VALIDATION_RUNNING", "VALIDATION_STOPPED", "VALIDATION_FAILED"],
    )
    fun `Given an rdf file import command, when rdfFile is not validated, it throws an exception`(state: State) {
        val rdfFile = createRdfFile().copy(state = state)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<RdfFileNotValidated> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file import command, when job service reports job already running, it throws an exception`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_DONE)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) } throws JobAlreadyRunning(JobId(123))

        shouldThrow<RdfFileImportAlreadyRunning> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_RUNNING)) }
    }

    @Test
    fun `Given an rdf file import command, when job service reports job restart failed, it throws an exception`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_DONE)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) } throws JobRestartFailed(JobId(123), RuntimeException())

        shouldThrow<RdfFileImportRestartFailed> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_FAILED)) }
    }

    @Test
    fun `Given an rdf file import command, when job service reports job already complete, it throws an exception`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_DONE)
        val command = ImportRdfFileUseCase.ImportCommand(
            id = rdfFile.id,
            contributorId = rdfFile.createdBy,
        )

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.save(any()) } just runs
        every { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) } throws JobAlreadyComplete(JobId(123))

        shouldThrow<RdfFileAlreadyImported> { service.import(command) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_QUEUED)) }
        verify(exactly = 1) { jobUseCases.runJob(JobNames.IMPORT_RDF_FILE, any()) }
        verify(exactly = 1) { repository.save(rdfFile.copy(state = State.IMPORT_DONE)) }
    }

    @Test
    fun `Given an rdf file delete command, when rdfFile exists, and the user is the owner, it deletes the rdfFile and all job results`() {
        val rdfFile = createRdfFile()

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { repository.deleteById(rdfFile.id) } just runs
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs

        service.deleteById(rdfFile.id, rdfFile.createdBy)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { repository.deleteById(rdfFile.id) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file delete command, when rdfFile exists, and the user is an admin, it deletes the rdfFile and all job results`() {
        val rdfFile = createRdfFile()
        val admin = createContributor(isAdmin = true)

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { repository.deleteById(rdfFile.id) } just runs
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs

        service.deleteById(rdfFile.id, admin.id)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { repository.deleteById(rdfFile.id) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file delete command, when rdfFile exists, and the user is not the owner and not an admin, it throws an exception`() {
        val rdfFile = createRdfFile()
        val otherUser = createContributor()

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<RdfFileNotFound> { service.deleteById(rdfFile.id, otherUser.id) }

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given an rdf file delete command, when rdfFile exists, and validation job is still running, it stops the validation job`() {
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_RUNNING, validationJobId = JobId(123))

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { jobUseCases.stopJob(rdfFile.validationJobId!!, rdfFile.createdBy) } just runs
        every { repository.deleteById(rdfFile.id) } just runs
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs

        service.deleteById(rdfFile.id, rdfFile.createdBy)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { jobUseCases.stopJob(rdfFile.validationJobId!!, rdfFile.createdBy) }
        verify(exactly = 1) { repository.deleteById(rdfFile.id) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
    }

    @Test
    fun `Given an rdf file delete command, when rdfFile exists, and import job is still running, it stops the import job`() {
        val rdfFile = createRdfFile().copy(state = State.IMPORT_RUNNING, importJobId = JobId(123))

        every { repository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { jobUseCases.stopJob(rdfFile.importJobId!!, rdfFile.createdBy) } just runs
        every { repository.deleteById(rdfFile.id) } just runs
        every { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) } just runs
        every { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) } just runs

        service.deleteById(rdfFile.id, rdfFile.createdBy)

        verify(exactly = 1) { repository.findById(rdfFile.id) }
        verify(exactly = 1) { jobUseCases.stopJob(rdfFile.importJobId!!, rdfFile.createdBy) }
        verify(exactly = 1) { repository.deleteById(rdfFile.id) }
        verify(exactly = 1) { rdfClassRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfPredicateRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfResourceRepository.deleteAllByRdfFileId(rdfFile.id) }
        verify(exactly = 1) { rdfTripleRepository.deleteAllByRdfFileId(rdfFile.id) }
    }
}
