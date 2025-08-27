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
import org.orkg.common.md5
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.dataimport.domain.csv.CSV.Format
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSV.Type
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.UpdateCSVUseCase
import org.orkg.dataimport.input.testing.fixtures.createCSVCommand
import org.orkg.dataimport.output.CSVRepository
import org.orkg.testing.MockUserId
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

internal class CSVServiceUnitTest : MockkBaseTest {
    private val repository: CSVRepository = mockk()
    private val jobUseCases: JobUseCases = mockk()
    private val contributorRepository: ContributorRepository = mockk()

    private val service = CSVService(
        repository,
        jobUseCases,
        contributorRepository,
        fixedClock,
    )

    @Test
    fun `Given a csv id and a contributor id, when fetching the csv, and the user is the owner, it returns the csv`() {
        val csv = createCSV()

        every { repository.findById(csv.id) } returns Optional.of(csv)

        service.findByIdAndCreatedBy(csv.id, csv.createdBy) shouldBe Optional.of(csv)

        verify(exactly = 1) { repository.findById(csv.id) }
    }

    @Test
    fun `Given a csv id and a contributor id, when fetching the csv, and the user is an admin, it returns the csv`() {
        val csv = createCSV()
        val admin = createContributor(isAdmin = true)

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)

        service.findByIdAndCreatedBy(csv.id, admin.id) shouldBe Optional.of(csv)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
    }

    @Test
    fun `Given a csv id and a contributor id, when fetching the csv, and the user is not the owner and not an admin, it returns an empty result`() {
        val csv = createCSV()
        val otherUser = createContributor()

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        service.findByIdAndCreatedBy(csv.id, otherUser.id).isEmpty shouldBe true

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given a csv create command, when inputs are valid, it creates a new csv`() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val command = createCSVCommand()

        mockkStatic(UUID::class) {
            every { repository.existsByDataMD5(command.data.md5) } returns false
            every { UUID.randomUUID() } returns id.value
            every { repository.save(any()) } just runs

            service.create(command) shouldBe id

            verify(exactly = 1) { repository.existsByDataMD5(command.data.md5) }
            verify(exactly = 1) { UUID.randomUUID() }
            verify(exactly = 1) {
                repository.save(
                    withArg {
                        it.id shouldBe id
                        it.name shouldBe command.name
                        it.type shouldBe command.type
                        it.format shouldBe command.format
                        it.state shouldBe State.UPLOADED
                        it.data shouldBe command.data
                        it.createdBy shouldBe command.contributorId
                        it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    }
                )
            }
        }
    }

    @Test
    fun `Given a csv create command, when data is blank, it throws an exception`() {
        val command = createCSVCommand().copy(data = "  ")

        shouldThrow<CSVCannotBeBlank> { service.create(command) }
    }

    @Test
    fun `Given a csv create command, when another csv with the same data already exists, it throws an exception`() {
        val command = createCSVCommand()

        every { repository.existsByDataMD5(command.data.md5) } returns true

        shouldThrow<CSVAlreadyExists> { service.create(command) }

        verify(exactly = 1) { repository.existsByDataMD5(command.data.md5) }
    }

    @Test
    fun `Given a csv update command, when updating all properties, and the user is the owner, it returns success`() {
        val csv = createCSV()
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = csv.createdBy,
            name = "changed",
            data = "changed",
            type = Type.PAPER,
            format = Format.EXCEL_COMMA_DELIMITED,
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { repository.existsByDataMD5(command.data!!.md5) } returns false
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { repository.existsByDataMD5(command.data!!.md5) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe csv.id
                    it.name shouldBe command.name
                    it.type shouldBe command.type
                    it.format shouldBe command.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe command.data
                    it.createdBy shouldBe csv.createdBy
                    it.createdAt shouldBe csv.createdAt
                }
            )
        }
    }

    @Test
    fun `Given a csv update command, when updating all properties, and the user is an admin, it returns success`() {
        val csv = createCSV()
        val admin = createContributor(isAdmin = true)
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = admin.id,
            name = "changed",
            data = "changed",
            type = Type.PAPER,
            format = Format.EXCEL_COMMA_DELIMITED,
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { repository.existsByDataMD5(command.data!!.md5) } returns false
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { repository.existsByDataMD5(command.data!!.md5) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe csv.id
                    it.name shouldBe command.name
                    it.type shouldBe command.type
                    it.format shouldBe command.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe command.data
                    it.createdBy shouldBe csv.createdBy
                    it.createdAt shouldBe csv.createdAt
                }
            )
        }
    }

    @Test
    fun `Given a csv update command, when updating all properties, and the user is not the owner and not an admin, it throws an exception`() {
        val csv = createCSV().copy(
            state = State.VALIDATION_DONE,
            validationJobId = JobId(123)
        )
        val otherUser = createContributor(isAdmin = false)
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = otherUser.id,
            data = "changed",
            type = Type.PAPER,
            format = Format.EXCEL_COMMA_DELIMITED,
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<CSVNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given a csv update command, when updating no properties, it does nothing`() {
        val csv = createCSV()
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = csv.createdBy
        )

        service.update(command)
    }

    @Test
    fun `Given a csv update command, when validation is currently running, it stops the job`() {
        val csv = createCSV().copy(
            state = State.VALIDATION_RUNNING,
            validationJobId = JobId(123)
        )
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = csv.createdBy,
            name = "changed",
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { jobUseCases.stopJob(csv.validationJobId!!, csv.createdBy) } just runs
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { jobUseCases.stopJob(csv.validationJobId!!, csv.createdBy) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe csv.id
                    it.name shouldBe command.name
                    it.type shouldBe csv.type
                    it.format shouldBe csv.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe csv.data
                    it.createdBy shouldBe csv.createdBy
                    it.createdAt shouldBe csv.createdAt
                }
            )
        }
    }

    @Test
    fun `Given a csv update command, when csv does not exist, it throws an exception`() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val command = UpdateCSVUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            data = "changed",
        )

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<CSVNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(id) }
    }

    @ParameterizedTest
    @EnumSource(State::class, names = arrayOf("IMPORT_RUNNING", "IMPORT_STOPPED", "IMPORT_FAILED", "IMPORT_DONE"))
    fun `Given a csv update command, when csv is already imported, it throws an exception`(state: State) {
        val csv = createCSV().copy(state = state)
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = csv.createdBy,
            data = "changed"
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)

        shouldThrow<CSVAlreadyImported> { service.update(command) }

        verify(exactly = 1) { repository.findById(csv.id) }
    }

    @Test
    fun `Given a csv update command, when another csv with the same data already exists, it throws an exception`() {
        val csv = createCSV()
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = csv.createdBy,
            data = "changed"
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { repository.existsByDataMD5(command.data!!.md5) } returns true

        shouldThrow<CSVAlreadyExists> { service.update(command) }

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { repository.existsByDataMD5(command.data!!.md5) }
    }

    @Test
    fun `Given a csv update command, when data is identical, it does not query the csv repository for duplicates`() {
        val csv = createCSV()
        val command = UpdateCSVUseCase.UpdateCommand(
            id = csv.id,
            contributorId = csv.createdBy,
            name = "changed",
            data = csv.data,
        )

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe csv.id
                    it.name shouldBe command.name
                    it.type shouldBe csv.type
                    it.format shouldBe csv.format
                    it.state shouldBe State.UPLOADED
                    it.data shouldBe csv.data
                    it.createdBy shouldBe csv.createdBy
                    it.createdAt shouldBe csv.createdAt
                }
            )
        }
    }

    @Test
    fun `Given a csv delete command, when csv exists, and the user is the owner, it deletes the csv and all job results`() {
        val csv = createCSV()

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { repository.deleteById(csv.id) } just runs

        service.deleteById(csv.id, csv.createdBy)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { repository.deleteById(csv.id) }
    }

    @Test
    fun `Given a csv delete command, when csv exists, and the user is an admin, it deletes the csv and all job results`() {
        val csv = createCSV()
        val admin = createContributor(isAdmin = true)

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { repository.deleteById(csv.id) } just runs

        service.deleteById(csv.id, admin.id)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { repository.deleteById(csv.id) }
    }

    @Test
    fun `Given a csv delete command, when csv exists, and the user is not the owner and not an admin, it throws an exception`() {
        val csv = createCSV()
        val otherUser = createContributor()

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<CSVNotFound> { service.deleteById(csv.id, otherUser.id) }

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given a csv delete command, when csv exists, and validation job is still running, it stops the validation job`() {
        val csv = createCSV().copy(state = State.VALIDATION_RUNNING, validationJobId = JobId(123))

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { jobUseCases.stopJob(csv.validationJobId!!, csv.createdBy) } just runs
        every { repository.deleteById(csv.id) } just runs

        service.deleteById(csv.id, csv.createdBy)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { jobUseCases.stopJob(csv.validationJobId!!, csv.createdBy) }
        verify(exactly = 1) { repository.deleteById(csv.id) }
    }

    @Test
    fun `Given a csv delete command, when csv exists, and import job is still running, it stops the import job`() {
        val csv = createCSV().copy(state = State.IMPORT_RUNNING, importJobId = JobId(123))

        every { repository.findById(csv.id) } returns Optional.of(csv)
        every { jobUseCases.stopJob(csv.importJobId!!, csv.createdBy) } just runs
        every { repository.deleteById(csv.id) } just runs

        service.deleteById(csv.id, csv.createdBy)

        verify(exactly = 1) { repository.findById(csv.id) }
        verify(exactly = 1) { jobUseCases.stopJob(csv.importJobId!!, csv.createdBy) }
        verify(exactly = 1) { repository.deleteById(csv.id) }
    }
}
