package org.orkg.graph.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.CuratorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.fixedClock
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

class ResourceServiceUnitTests {

    private val repository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val curatorRepository: CuratorRepository = mockk()

    private val service = ResourceService(
        repository,
        statementRepository,
        classRepository,
        curatorRepository,
        fixedClock,
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            repository,
            statementRepository,
            classRepository,
            curatorRepository
        )
    }

    @Test
    fun `given a resource create command, when inputs are valid, it creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            label = "label",
            classes = setOf(Classes.paper),
            extractionMethod = ExtractionMethod.MANUAL,
            contributorId = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId("1255bbe4-1850-4033-ba10-c80d4b370e3e"),
            organizationId = OrganizationId("56a4b65e-de56-0d4b-255b-255b372b65ef"),
            modifiable = false
        )

        every { repository.findById(id) } returns Optional.empty()
        every { classRepository.existsAll(command.classes) } returns true
        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { classRepository.existsAll(command.classes) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe command.id
                it.label shouldBe command.label
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.classes shouldBe command.classes
                it.createdBy shouldBe command.contributorId
                it.observatoryId shouldBe command.observatoryId
                it.extractionMethod shouldBe command.extractionMethod
                it.organizationId shouldBe command.organizationId
                it.visibility shouldBe Visibility.DEFAULT
                it.verified shouldBe null
                it.unlistedBy shouldBe null
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `given a resource create command, when inputs are minimal, it get a new id form the repository and creates a new resource with default values`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            label = "label"
        )

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe id
                it.label shouldBe command.label
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.classes shouldBe command.classes
                it.createdBy shouldBe ContributorId.UNKNOWN
                it.observatoryId shouldBe ObservatoryId.UNKNOWN
                it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                it.organizationId shouldBe OrganizationId.UNKNOWN
                it.visibility shouldBe Visibility.DEFAULT
                it.verified shouldBe null
                it.unlistedBy shouldBe null
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `given a resource create command, when label is invalid, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            label = "\n",
            classes = setOf(Classes.paper)
        )

        assertThrows<InvalidLabel> { service.create(command) }
    }

    @Test
    fun `given a resource create command, when reserved class is specified in class list, it throws an exception`() {
        val command = CreateResourceUseCase.CreateCommand(
            label = "label",
            classes = setOf(reservedClassIds.first())
        )

        assertThrows<ReservedClass> { service.create(command) }
    }

    @Test
    fun `given a resource create command, when specified class does not exist, it throws an exception`() {
        val command = CreateResourceUseCase.CreateCommand(
            label = "label",
            classes = setOf(Classes.paper)
        )

        every { classRepository.existsAll(command.classes) } returns false

        assertThrows<InvalidClassCollection> { service.create(command) }

        verify(exactly = 1) { classRepository.existsAll(command.classes) }
    }

    @Test
    fun `given a resource create command, when using unsafe creation method, it creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            label = "label",
            classes = setOf(Classes.paper),
            extractionMethod = ExtractionMethod.MANUAL,
            contributorId = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId("1255bbe4-1850-4033-ba10-c80d4b370e3e"),
            organizationId = OrganizationId("56a4b65e-de56-0d4b-255b-255b372b65ef"),
            modifiable = false
        )

        every { repository.save(any()) } just runs

        service.createUnsafe(command) shouldBe id

        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe command.id
                it.label shouldBe command.label
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.classes shouldBe command.classes
                it.createdBy shouldBe command.contributorId
                it.observatoryId shouldBe command.observatoryId
                it.extractionMethod shouldBe command.extractionMethod
                it.organizationId shouldBe command.organizationId
                it.visibility shouldBe Visibility.DEFAULT
                it.verified shouldBe null
                it.unlistedBy shouldBe null
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `given a resource create command, when using unsafe creation method with minimal inputs, it assigns a new id and creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            label = "label"
        )

        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } just runs

        service.createUnsafe(command) shouldBe id

        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(withArg {
                it.id shouldBe id
                it.label shouldBe command.label
                it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                it.classes shouldBe command.classes
                it.createdBy shouldBe ContributorId.UNKNOWN
                it.observatoryId shouldBe ObservatoryId.UNKNOWN
                it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                it.organizationId shouldBe OrganizationId.UNKNOWN
                it.visibility shouldBe Visibility.DEFAULT
                it.verified shouldBe null
                it.unlistedBy shouldBe null
                it.modifiable shouldBe command.modifiable
            })
        }
    }

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is found, it returns success`() {
        val id = ThingId("R123")
        val resource = createResource(id = id)
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.of(resource)
        every { statementRepository.findTimelineByResourceId(id, pageable) } returns Page.empty()

        service.findTimelineByResourceId(id, pageable)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { statementRepository.findTimelineByResourceId(any(), any()) }
    }

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            service.findTimelineByResourceId(id, pageable)
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given all contributors for a resource are being retrieved, when the resource is found, it returns all creators`() {
        val id = ThingId("R123")
        val resource = createResource(id = id)
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.of(resource)
        every { statementRepository.findAllContributorsByResourceId(id, pageable) } returns Page.empty()

        service.findAllContributorsByResourceId(id, pageable)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { statementRepository.findAllContributorsByResourceId(any(), any()) }
    }

    @Test
    fun `given all contributors for a resource are being retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            service.findAllContributorsByResourceId(id, pageable)
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a resource is being deleted, when it is still used in a statement, an appropriate error is thrown`() {
        val mockResource = createResource()
        val couldBeAnyone = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { statementRepository.checkIfResourceHasStatements(mockResource.id) } returns true

        shouldThrow<ResourceUsedInStatement> {
            service.delete(mockResource.id, couldBeAnyone)
        }

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { statementRepository.checkIfResourceHasStatements(mockResource.id) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, and it is owned by the user, it gets deleted`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val theOwningContributor = createContributor(id = theOwningContributorId)
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { statementRepository.checkIfResourceHasStatements(mockResource.id) } returns false
        every { curatorRepository.findById(theOwningContributorId) } returns theOwningContributor
        every { repository.deleteById(mockResource.id) } returns Unit

        service.delete(mockResource.id, theOwningContributorId)

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { statementRepository.checkIfResourceHasStatements(mockResource.id) }
        verify(exactly = 1) { repository.deleteById(mockResource.id) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, and it is not owned by the user, but the user is a curator, it gets deleted`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val aCurator = createContributor(id = ContributorId("645fabd1-9952-41f8-9239-627ee67c1940"))
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { statementRepository.checkIfResourceHasStatements(mockResource.id) } returns false
        every { curatorRepository.findById(aCurator.id) } returns aCurator
        every { repository.deleteById(mockResource.id) } returns Unit

        service.delete(mockResource.id, aCurator.id)

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { statementRepository.checkIfResourceHasStatements(mockResource.id) }
        verify(exactly = 1) { repository.deleteById(mockResource.id) }
        verify(exactly = 1) { curatorRepository.findById(aCurator.id) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, and it is not owned by the user, and the user is not a curator, it gets deleted`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val loggedInUserId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { statementRepository.checkIfResourceHasStatements(mockResource.id) } returns false
        every { curatorRepository.findById(loggedInUserId) } returns null
        every { repository.deleteById(mockResource.id) } returns Unit

        shouldThrow<NeitherOwnerNorCurator> {
            service.delete(mockResource.id, loggedInUserId)
        }

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { statementRepository.checkIfResourceHasStatements(mockResource.id) }
        verify(exactly = 0) { repository.deleteById(mockResource.id) }
        verify(exactly = 1) { curatorRepository.findById(loggedInUserId) }
    }

    @Test
    fun `given a resource is being deleted, when resource is unmodifiable, an appropriate error is thrown`() {
        val mockResource = createResource(modifiable = false)
        val loggedInUser = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)

        shouldThrow<ResourceNotModifiable> {
            service.delete(mockResource.id, loggedInUser)
        }

        verify(exactly = 1) { repository.findById(mockResource.id) }
    }

    @Test
    fun `Given a resource update command, when it contains a reserved class, it throws an exception`() {
        val resource = createResource()
        val classes = setOf(Classes.list)

        every { repository.findById(resource.id) } returns Optional.of(resource)

        assertThrows<ReservedClass> {
            service.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = resource.id,
                    classes = classes
                )
            )
        }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when updating all properties, it returns success`() {
        val resource = createResource()
        val label = "updated label"
        val classes = setOf(Classes.paper)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.AUTOMATIC
        val modifiable = false

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { classRepository.existsAll(classes) } returns true
        every { repository.save(any()) } just runs

        service.update(
            UpdateResourceUseCase.UpdateCommand(
                resource.id, label, classes, observatoryId, organizationId, extractionMethod, modifiable
            )
        )

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { classRepository.existsAll(classes) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.label shouldBe label
                it.classes shouldBe classes
                it.observatoryId shouldBe observatoryId
                it.organizationId shouldBe organizationId
                it.extractionMethod shouldBe extractionMethod
                it.modifiable shouldBe modifiable
            })
        }
    }

    @Test
    fun `Given a resource update command, when updating no properties, it returns success`() {
        val resource = createResource()

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(UpdateResourceUseCase.UpdateCommand(resource.id))

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) {
            repository.save(withArg {
                it.label shouldBe resource.label
                it.classes shouldBe resource.classes
                it.observatoryId shouldBe resource.observatoryId
                it.organizationId shouldBe resource.organizationId
                it.extractionMethod shouldBe resource.extractionMethod
                it.modifiable shouldBe resource.modifiable
            })
        }
    }

    @Test
    fun `Given a resource update command, when updating an unmodifiable resource, it throws an exception`() {
        val resource = createResource(modifiable = false)
        val label = "updated label"
        val classes = setOf(Classes.paper)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.AUTOMATIC
        val modifiable = false

        every { repository.findById(resource.id) } returns Optional.of(resource)

        shouldThrow<ResourceNotModifiable> {
            service.update(
                UpdateResourceUseCase.UpdateCommand(
                    resource.id, label, classes, observatoryId, organizationId, extractionMethod, modifiable
                )
            )
        }.message shouldBe """Resource "${resource.id}" is not modifiable."""

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when it contains an invalid label, it throws an exception`() {
        val resource = createResource()

        every { repository.findById(resource.id) } returns Optional.of(resource)

        assertThrows<InvalidLabel> {
            service.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = resource.id,
                    label = "\n"
                )
            )
        }

        verify(exactly = 1) { repository.findById(resource.id) }
    }
}
