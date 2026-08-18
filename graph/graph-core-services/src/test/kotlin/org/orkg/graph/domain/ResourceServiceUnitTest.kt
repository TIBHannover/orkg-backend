package org.orkg.graph.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.community.testing.fixtures.createOrganization
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

internal class ResourceServiceUnitTest : MockkBaseTest {
    private val repository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()
    private val classRepository: ClassRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val observatoryRepository: ObservatoryRepository = mockk()
    private val organizationRepository: OrganizationRepository = mockk()

    private val service = ResourceService(
        repository,
        statementRepository,
        classRepository,
        contributorRepository,
        thingRepository,
        unsafeResourceUseCases,
        observatoryRepository,
        organizationRepository,
    )

    @Test
    fun `given a resource create command, when inputs are valid, it creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            classes = setOf(Classes.paper),
            extractionMethod = ExtractionMethod.MANUAL,
            observatoryId = ObservatoryId("1255bbe4-1850-4033-ba10-c80d4b370e3e"),
            organizationId = OrganizationId("56a4b65e-de56-0d4b-255b-255b372b65ef"),
            uri = IRI.create("https://orkg.org/predicate/P1"),
            modifiable = false,
        )

        every { thingRepository.findById(id) } returns Optional.empty()
        every { classRepository.existsAllById(command.classes) } returns true
        every { thingRepository.findAll(PageRequests.SINGLE, uri = command.uri) } returns pageOf()
        every { unsafeResourceUseCases.create(command) } returns id

        service.create(command) shouldBe id

        verify(exactly = 1) { thingRepository.findById(id) }
        verify(exactly = 1) { classRepository.existsAllById(command.classes) }
        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = command.uri) }
        verify(exactly = 1) { unsafeResourceUseCases.create(command) }
    }

    @Test
    fun `given a resource create command, when inputs are minimal, it creates a new resource`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
        )

        every { unsafeResourceUseCases.create(command) } returns id

        service.create(command) shouldBe id

        verify(exactly = 1) { unsafeResourceUseCases.create(command) }
    }

    @Test
    fun `given a resource create command, when label is invalid, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "\n",
            classes = setOf(Classes.paper),
        )

        assertThrows<InvalidLabel> { service.create(command) }
    }

    @Test
    fun `given a resource create command, when reserved class is specified in class list, it throws an exception`() {
        val command = CreateResourceUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            classes = setOf(reservedClassIds.first()),
        )

        assertThrows<ReservedClassId> { service.create(command) }
    }

    @Test
    fun `given a resource create command, when specified class does not exist, it throws an exception`() {
        val command = CreateResourceUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
            classes = setOf(Classes.paper),
        )

        every { classRepository.existsAllById(command.classes) } returns false

        assertThrows<InvalidClassCollection> { service.create(command) }

        verify(exactly = 1) { classRepository.existsAllById(command.classes) }
    }

    @Test
    fun `Given a resource create command, when uri is not absolute, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "some label",
            uri = IRI.create("invalid"),
        )

        assertThrows<URINotAbsolute> { service.create(command) }
    }

    @Test
    fun `Given a resource create command, when uri is already in use, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "some label",
            uri = IRI.create("https://example.com/"),
        )

        every { thingRepository.findAll(PageRequests.SINGLE, uri = command.uri) } returns pageOf(createResource())

        assertThrows<URIAlreadyInUse> { service.create(command) }

        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = command.uri) }
    }

    @Test
    fun `given a resource create command, when id already exists, it throws an exception`() {
        val id = ThingId("R123")
        val command = CreateResourceUseCase.CreateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "label",
        )

        every { thingRepository.findById(id) } returns Optional.of(createResource(id))

        assertThrows<ThingAlreadyExists> { service.create(command) }

        verify(exactly = 1) { thingRepository.findById(id) }
    }

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is found, it returns success`() {
        val id = ThingId("R123")
        val resource = createResource(id = id)
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.of(resource)
        every { statementRepository.findTimelineById(id, pageable) } returns Page.empty()

        service.findTimelineById(id, pageable)

        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { statementRepository.findTimelineById(any(), any()) }
    }

    @Test
    fun `given a timeline for a resource is retrieved, when the resource is not found, then an exception is thrown`() {
        val id = ThingId("R123")
        val pageable = PageRequest.of(0, 5)

        every { repository.findById(id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> {
            service.findTimelineById(id, pageable)
        }

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `given a resource is being deleted, when it is still used in a statement, an appropriate error is thrown`() {
        val mockResource = createResource()
        val couldBeAnyone = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { thingRepository.isUsedAsObject(mockResource.id) } returns true

        shouldThrow<ResourceInUse> {
            service.delete(mockResource.id, couldBeAnyone)
        }

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(mockResource.id) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, and it is owned by the user, it gets deleted`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { thingRepository.isUsedAsObject(mockResource.id) } returns false
        every { unsafeResourceUseCases.delete(mockResource.id, theOwningContributorId) } returns Unit

        service.delete(mockResource.id, theOwningContributorId)

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(mockResource.id) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(mockResource.id, theOwningContributorId) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, and it is not owned by the user, but the user is a curator, it gets deleted`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val aCurator = createContributor(id = ContributorId("645fabd1-9952-41f8-9239-627ee67c1940"), isCurator = true)
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { thingRepository.isUsedAsObject(mockResource.id) } returns false
        every { contributorRepository.findById(aCurator.id) } returns Optional.of(aCurator)
        every { unsafeResourceUseCases.delete(mockResource.id, aCurator.id) } returns Unit

        service.delete(mockResource.id, aCurator.id)

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(mockResource.id) }
        verify(exactly = 1) { unsafeResourceUseCases.delete(mockResource.id, aCurator.id) }
        verify(exactly = 1) { contributorRepository.findById(aCurator.id) }
    }

    @Test
    fun `given a resource is being deleted, when it is not used in a statement, and it is not owned by the user, and the user is not a curator, it throws an exception`() {
        val theOwningContributorId = ContributorId("1255bbe4-1850-4033-ba10-c80d4b370e3e")
        val loggedInUserId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val loggedInUser = createContributor(id = loggedInUserId)
        val mockResource = createResource(createdBy = theOwningContributorId)

        every { repository.findById(mockResource.id) } returns Optional.of(mockResource)
        every { thingRepository.isUsedAsObject(mockResource.id) } returns false
        every { contributorRepository.findById(loggedInUserId) } returns Optional.of(loggedInUser)

        shouldThrow<NeitherOwnerNorCurator> {
            service.delete(mockResource.id, loggedInUserId)
        }

        verify(exactly = 1) { repository.findById(mockResource.id) }
        verify(exactly = 1) { thingRepository.isUsedAsObject(mockResource.id) }
        verify(exactly = 1) { contributorRepository.findById(loggedInUserId) }
        verify(exactly = 0) { unsafeResourceUseCases.delete(mockResource.id, loggedInUserId) }
    }

    @Test
    fun `Given a resource update command, when resource does not exist, it throws an exception`() {
        val command = UpdateResourceUseCase.UpdateCommand(
            id = ThingId("R123"),
            contributorId = ContributorId(MockUserId.USER),
            label = "new label",
        )

        every { repository.findById(any()) } returns Optional.empty()

        shouldThrow<ResourceNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(any()) }
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
    fun `Given a resource update command, when updating no properties, it does nothing`() {
        val id = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        service.update(UpdateResourceUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a resource update command, when it contains an invalid label, it throws an exception`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            label = "\n",
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)

        shouldThrow<InvalidLabel> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when updating the extraction method with an invalid transition, it throws an exception`() {
        val resource = createResource(extractionMethod = ExtractionMethod.AI_GENERATED)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            extractionMethod = ExtractionMethod.UNKNOWN,
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)

        shouldThrow<InvalidExtractionMethodChange> { service.update(command) }.asClue {
            it.message shouldBe """Extraction method "${ExtractionMethod.AI_GENERATED}" cannot be changed to "${ExtractionMethod.UNKNOWN}"."""
        }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when it contains a reserved class, it throws an exception`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            classes = setOf(Classes.list),
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)

        shouldThrow<ReservedClassId> { service.update(command) }.asClue {
            it.message shouldBe """Class id "${Classes.list}" is reserved."""
        }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when uri would be reset, it throws an exception`() {
        val id = ThingId("R123")
        val resource = createResource(id = id, uri = IRI.create("https://example.org/OK"))
        val command = UpdateResourceUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            uri = IRI.create("https://exmaple.org/changed"),
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)

        assertThrows<CannotResetURI> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when uri is not absolute, it throws an exception`() {
        val id = ThingId("R123")
        val resource = createResource(id = id, uri = null)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            uri = IRI.create("invalid"),
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)

        assertThrows<URINotAbsolute> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when uri is already in use, it throws an exception`() {
        val id = ThingId("R123")
        val resource = createResource(id = id, uri = null)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = id,
            contributorId = ContributorId(MockUserId.USER),
            label = "some label",
            uri = IRI.create("https://example.com/"),
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { thingRepository.findAll(PageRequests.SINGLE, uri = command.uri) } returns pageOf(createResource())

        assertThrows<URIAlreadyInUse> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = command.uri) }
    }

    @Test
    fun `Given a resource update command, when updating an unmodifiable resource, it throws an exception`() {
        val resource = createResource(modifiable = false)
        val command = UpdateResourceUseCase.UpdateCommand(resource.id, ContributorId(MockUserId.USER), label = "new label")

        every { repository.findById(resource.id) } returns Optional.of(resource)

        shouldThrow<ResourceNotModifiable> { service.update(command) }.asClue {
            it.message shouldBe """Resource "${resource.id}" is not modifiable."""
        }

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when updating the visibility to deleted as a curator, it returns success`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val curatorId = ContributorId(MockUserId.CURATOR)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = curatorId,
            visibility = Visibility.DELETED,
        )
        val curator = createContributor(curatorId, isCurator = true)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(curatorId) } returns Optional.of(curator)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(curatorId) }
        verify(exactly = 1) { repository.save(withArg { it.visibility shouldBe Visibility.DELETED }) }
    }

    @Test
    fun `Given a resource update command, when updating the visibility to deleted as the owner, it returns success`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = resource.createdBy,
            visibility = Visibility.DELETED,
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.visibility shouldBe Visibility.DELETED }) }
    }

    @Test
    fun `Given a resource update command, when updating the visibility to deleted as some user, it throws an exception`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val contributorId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = contributorId,
            visibility = Visibility.DELETED,
        )
        val someUser = createContributor(contributorId, isCurator = false)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(contributorId) } returns Optional.of(someUser)

        shouldThrow<NeitherOwnerNorCurator> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a resource update command, when updating the visibility to featured as a curator, it returns success`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val curatorId = ContributorId(MockUserId.CURATOR)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = curatorId,
            visibility = Visibility.FEATURED,
        )
        val curator = createContributor(curatorId, isCurator = true)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(curatorId) } returns Optional.of(curator)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(curatorId) }
        verify(exactly = 1) { repository.save(withArg { it.visibility shouldBe Visibility.FEATURED }) }
    }

    @Test
    fun `Given a resource update command, when updating the visibility to featured as the owner, it throws an exception`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = resource.createdBy,
            visibility = Visibility.FEATURED,
        )
        val owner = createContributor(resource.createdBy)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(command.contributorId) } returns Optional.of(owner)

        shouldThrow<NeitherOwnerNorCurator> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(command.contributorId) }
    }

    @Test
    fun `Given a resource update command, when updating the visibility to featured as some user, it throws an exception`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val contributorId = ContributorId("89b13df4-22ae-4685-bed0-4bb1f1873c78")
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = contributorId,
            visibility = Visibility.FEATURED,
        )
        val someUser = createContributor(contributorId, isCurator = false)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(contributorId) } returns Optional.of(someUser)

        shouldThrow<NeitherOwnerNorCurator> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a resource update command, when updating the verified flag as a user, it throws an exception`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = contributorId,
            verified = true,
        )
        val someUser = createContributor(contributorId, isCurator = false)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(contributorId) } returns Optional.of(someUser)

        shouldThrow<NotACurator> { service.update(command) }.asClue {
            it.message shouldBe """Cannot change verified status: Contributor <$contributorId> is not a curator."""
        }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
    }

    @Test
    fun `Given a resource update command, when updating the verified flag as a curator, it returns success`() {
        val resource = createResource(createdBy = ContributorId(MockUserId.USER))
        val curatorId = ContributorId(MockUserId.CURATOR)
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = curatorId,
            verified = true,
        )
        val curator = createContributor(curatorId, isCurator = true)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(curatorId) } returns Optional.of(curator)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(curatorId) }
        verify(exactly = 1) { repository.save(withArg { it.verified shouldBe true }) }
    }

    @Test
    fun `Given a resource update command, when observatory does not exist, it throws an exception`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId("04c4d2f9-82e0-47c3-b0ef-79c1f515a940"),
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { observatoryRepository.existsById(command.observatoryId!!) } returns false

        shouldThrow<ObservatoryNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { observatoryRepository.existsById(command.observatoryId!!) }
    }

    @Test
    fun `Given a resource update command, when observatory is unknown, is does not throw an exception`() {
        val resource = createResource(observatoryId = ObservatoryId("04c4d2f9-82e0-47c3-b0ef-79c1f515a940"))
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId.UNKNOWN,
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        shouldNotThrow<ObservatoryNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.observatoryId shouldBe ObservatoryId.UNKNOWN }) }
    }

    @Test
    fun `Given a resource update command, when organization does not exist, it throws an exception`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            organizationId = OrganizationId("04c4d2f9-82e0-47c3-b0ef-79c1f515a940"),
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { organizationRepository.findById(command.organizationId!!) } returns Optional.empty()

        shouldThrow<OrganizationNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { organizationRepository.findById(command.organizationId!!) }
    }

    @Test
    fun `Given a resource update command, when organization is unknown, is does not throw an exception`() {
        val resource = createResource(organizationId = OrganizationId("04c4d2f9-82e0-47c3-b0ef-79c1f515a940"))
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.USER),
            organizationId = OrganizationId.UNKNOWN,
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        shouldNotThrow<OrganizationNotFound> { service.update(command) }

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.organizationId shouldBe OrganizationId.UNKNOWN }) }
    }

    @Test
    fun `Given a resource update command, when visibility is changed to unlisted, it sets the unlisted by metadata`() {
        val resource = createResource()
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.CURATOR),
            visibility = Visibility.UNLISTED,
        )
        val curator = createContributor(command.contributorId, isCurator = true)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(curator.id) } returns Optional.of(curator)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(curator.id) }
        verify(exactly = 1) { repository.save(withArg { it.unlistedBy shouldBe command.contributorId }) }
    }

    @Test
    fun `Given a resource update command, when visibility is changed from unlisted to something else, it clears the unlisted by metadata`() {
        val resource = createResource(
            visibility = Visibility.UNLISTED,
            unlistedBy = ContributorId(MockUserId.USER),
        )
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.CURATOR),
            visibility = Visibility.DEFAULT,
        )
        val curator = createContributor(command.contributorId, isCurator = true)

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { contributorRepository.findById(curator.id) } returns Optional.of(curator)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { contributorRepository.findById(curator.id) }
        verify(exactly = 1) { repository.save(withArg { it.unlistedBy shouldBe null }) }
    }

    @Test
    fun `Given a resource update command, when visibility is changed from unlisted to unlisted, it keeps the unlisted by metadata`() {
        val resource = createResource(
            visibility = Visibility.UNLISTED,
            unlistedBy = ContributorId(MockUserId.ADMIN),
        )
        val command = UpdateResourceUseCase.UpdateCommand(
            id = resource.id,
            contributorId = ContributorId(MockUserId.CURATOR),
            label = "some change",
            visibility = Visibility.UNLISTED,
        )

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { repository.save(any()) } just runs

        service.update(command)

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { repository.save(withArg { it.unlistedBy shouldBe resource.unlistedBy }) }
    }

    @Test
    fun `Given a resource update command, when updating with the same values, it does nothing`() {
        val resource = createResource()
        val contributorId = ContributorId(MockUserId.USER)

        every { repository.findById(resource.id) } returns Optional.of(resource)

        service.update(
            UpdateResourceUseCase.UpdateCommand(
                id = resource.id,
                contributorId = contributorId,
                label = resource.label,
                classes = resource.classes,
                observatoryId = resource.observatoryId,
                organizationId = resource.organizationId,
                extractionMethod = resource.extractionMethod,
                modifiable = resource.modifiable,
                visibility = resource.visibility,
                verified = resource.verified,
            ),
        )

        verify(exactly = 1) { repository.findById(resource.id) }
    }

    @Test
    fun `Given a resource update command, when updating all properties, it returns success`() {
        val resource = createResource(uri = null)
        val contributorId = ContributorId(MockUserId.USER)
        val contributor = createContributor(contributorId, isCurator = true)
        val label = "updated label"
        val classes = setOf(Classes.paper)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val extractionMethod = ExtractionMethod.AUTOMATIC
        val uri = IRI.create("https://orkg.org/predicates/R1")
        val modifiable = false
        val visibility = Visibility.FEATURED
        val verified = true

        every { repository.findById(resource.id) } returns Optional.of(resource)
        every { classRepository.existsAllById(classes) } returns true
        every { thingRepository.findAll(PageRequests.SINGLE, uri = uri) } returns pageOf()
        every { contributorRepository.findById(contributorId) } returns Optional.of(contributor)
        every { observatoryRepository.existsById(observatoryId) } returns true
        every { organizationRepository.findById(organizationId) } returns Optional.of(createOrganization(organizationId))
        every { repository.save(any()) } just runs

        service.update(
            UpdateResourceUseCase.UpdateCommand(
                id = resource.id,
                contributorId = contributorId,
                label = label,
                classes = classes,
                uri = uri,
                observatoryId = observatoryId,
                organizationId = organizationId,
                extractionMethod = extractionMethod,
                modifiable = modifiable,
                visibility = visibility,
                verified = verified,
            ),
        )

        verify(exactly = 1) { repository.findById(resource.id) }
        verify(exactly = 1) { classRepository.existsAllById(classes) }
        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = uri) }
        verify(exactly = 1) { contributorRepository.findById(contributorId) }
        verify(exactly = 1) { observatoryRepository.existsById(observatoryId) }
        verify(exactly = 1) { organizationRepository.findById(organizationId) }
        verify(exactly = 1) {
            repository.save(
                withArg {
                    it.id shouldBe resource.id
                    it.label shouldBe label
                    it.createdAt shouldBe resource.createdAt
                    it.classes shouldBe classes
                    it.uri shouldBe uri
                    it.createdBy shouldBe resource.createdBy
                    it.observatoryId shouldBe observatoryId
                    it.extractionMethod shouldBe extractionMethod
                    it.organizationId shouldBe organizationId
                    it.visibility shouldBe visibility
                    it.verified shouldBe verified
                    it.unlistedBy shouldBe resource.unlistedBy
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given a resource update command, when updating a rosetta stone statement resource, it throws an exception`() {
        val resource = createResource(classes = setOf(Classes.rosettaStoneStatement))
        val command = UpdateResourceUseCase.UpdateCommand(resource.id, ContributorId(MockUserId.USER), label = "new label")

        every { repository.findById(resource.id) } returns Optional.of(resource)

        shouldThrow<RosettaStoneStatementResourceNotModifiable> { service.update(command) }.asClue {
            it.message shouldBe """A rosetta stone statement resource cannot be managed using the resources endpoint. Please see the documentation on how to manage rosetta stone statements."""
        }

        verify(exactly = 1) { repository.findById(resource.id) }
    }
}
