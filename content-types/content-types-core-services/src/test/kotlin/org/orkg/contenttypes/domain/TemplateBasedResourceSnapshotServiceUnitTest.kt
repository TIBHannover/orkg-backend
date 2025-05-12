package org.orkg.contenttypes.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Handle
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createTemplateInstance
import org.orkg.contenttypes.input.CreateTemplateBasedResourceSnapshotUseCase
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.output.HandleService
import org.orkg.contenttypes.output.TemplateBasedResourceSnapshotRepository
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import java.net.URI
import java.time.OffsetDateTime
import java.util.Optional

internal class TemplateBasedResourceSnapshotServiceUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val templateUseCase: TemplateUseCases = mockk()
    private val templateInstanceUseCases: TemplateInstanceUseCases = mockk()
    private val templateBasedResourceSnapshotRepository: TemplateBasedResourceSnapshotRepository = mockk()
    private val handleService: HandleService = mockk()
    private val snapshotIdGenerator: SnapshotIdGenerator = mockk()
    private val urlTemplate: String = "http://orkg.org/resources/{id}/snapshots/{snapshotId}"

    private val service = TemplateBasedResourceSnapshotService(
        resourceRepository,
        templateUseCase,
        templateInstanceUseCases,
        templateBasedResourceSnapshotRepository,
        handleService,
        snapshotIdGenerator,
        urlTemplate,
        fixedClock,
    )

    @Test
    fun `Given a template instance, when creating a snapshot with handle, it creates a new template based resource snapshot and registers a handle`() {
        val template = createTemplate()
        val resource = createResource().copy(classes = setOf(template.targetClass.id))
        val templateInstance = createTemplateInstance()
        val id = SnapshotId("1a2b3c")
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
            resourceId = resource.id,
            templateId = template.id,
            contributorId = contributorId,
            registerHandle = true
        )
        val handle = Handle.of("20.154665/1a2b3c")

        every { resourceRepository.findById(resource.id) } returns Optional.of(resource)
        every { templateUseCase.findById(template.id) } returns Optional.of(template)
        every { templateInstanceUseCases.findById(template.id, resource.id) } returns Optional.of(templateInstance)
        every { snapshotIdGenerator.nextIdentity() } returns id
        every { handleService.register(any()) } returns handle
        every { templateBasedResourceSnapshotRepository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { resourceRepository.findById(resource.id) }
        verify(exactly = 1) { templateUseCase.findById(template.id) }
        verify(exactly = 1) { templateInstanceUseCases.findById(template.id, resource.id) }
        verify(exactly = 1) { snapshotIdGenerator.nextIdentity() }
        verify(exactly = 1) {
            handleService.register(
                withArg {
                    it.url shouldBe URI.create("http://orkg.org/resources/${resource.id}/snapshots/$id")
                    it.suffix shouldBe id.value
                }
            )
        }
        verify(exactly = 1) {
            templateBasedResourceSnapshotRepository.save(
                withArg {
                    it.id shouldBe id
                    it.createdBy shouldBe command.contributorId
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.data shouldBe templateInstance
                    it.resourceId shouldBe resource.id
                    it.templateId shouldBe template.id
                    it.handle shouldBe handle
                }
            )
        }
    }

    @Test
    fun `Given a template instance, when creating a snapshot without handle, it creates a new template based resource snapshot`() {
        val template = createTemplate()
        val resource = createResource().copy(classes = setOf(template.targetClass.id))
        val templateInstance = createTemplateInstance()
        val id = SnapshotId("1a2b3c")
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
            resourceId = resource.id,
            templateId = template.id,
            contributorId = contributorId,
            registerHandle = false
        )

        every { resourceRepository.findById(resource.id) } returns Optional.of(resource)
        every { templateUseCase.findById(template.id) } returns Optional.of(template)
        every { templateInstanceUseCases.findById(template.id, resource.id) } returns Optional.of(templateInstance)
        every { snapshotIdGenerator.nextIdentity() } returns id
        every { templateBasedResourceSnapshotRepository.save(any()) } just runs

        service.create(command) shouldBe id

        verify(exactly = 1) { resourceRepository.findById(resource.id) }
        verify(exactly = 1) { templateUseCase.findById(template.id) }
        verify(exactly = 1) { templateInstanceUseCases.findById(template.id, resource.id) }
        verify(exactly = 1) { snapshotIdGenerator.nextIdentity() }
        verify(exactly = 1) {
            templateBasedResourceSnapshotRepository.save(
                withArg {
                    it.id shouldBe id
                    it.createdBy shouldBe command.contributorId
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.data shouldBe templateInstance
                    it.resourceId shouldBe resource.id
                    it.templateId shouldBe template.id
                    it.handle shouldBe null
                }
            )
        }
    }

    @Test
    fun `Given a template instance, when creating a snapshot, and resource can not be found, it throws an exception`() {
        val template = createTemplate()
        val resource = createResource()
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
            resourceId = resource.id,
            templateId = template.id,
            contributorId = contributorId,
            registerHandle = false
        )

        every { resourceRepository.findById(resource.id) } returns Optional.empty()

        shouldThrow<ResourceNotFound> { service.create(command) }

        verify(exactly = 1) { resourceRepository.findById(resource.id) }
    }

    @Test
    fun `Given a template instance, when creating a snapshot, and template can not be found, it throws an exception`() {
        val template = createTemplate()
        val resource = createResource()
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
            resourceId = resource.id,
            templateId = template.id,
            contributorId = contributorId,
            registerHandle = false
        )

        every { resourceRepository.findById(resource.id) } returns Optional.of(resource)
        every { templateUseCase.findById(template.id) } returns Optional.empty()

        shouldThrow<TemplateNotFound> { service.create(command) }

        verify(exactly = 1) { resourceRepository.findById(resource.id) }
        verify(exactly = 1) { templateUseCase.findById(template.id) }
    }

    @Test
    fun `Given a template instance, when creating a snapshot, and target resource is not an instance of the template, it throws an exception`() {
        val template = createTemplate()
        val resource = createResource()
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
            resourceId = resource.id,
            templateId = template.id,
            contributorId = contributorId,
            registerHandle = false
        )

        every { resourceRepository.findById(resource.id) } returns Optional.of(resource)
        every { templateUseCase.findById(template.id) } returns Optional.of(template)

        shouldThrow<TemplateNotApplicable> { service.create(command) }

        verify(exactly = 1) { resourceRepository.findById(resource.id) }
        verify(exactly = 1) { templateUseCase.findById(template.id) }
    }

    @Test
    fun `Given a template instance, when creating a snapshot, and template instance can not be found, it throws an exception`() {
        val template = createTemplate()
        val resource = createResource().copy(classes = setOf(template.targetClass.id))
        val contributorId = ContributorId(MockUserId.USER)
        val command = CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
            resourceId = resource.id,
            templateId = template.id,
            contributorId = contributorId,
            registerHandle = false
        )

        every { resourceRepository.findById(resource.id) } returns Optional.of(resource)
        every { templateUseCase.findById(template.id) } returns Optional.of(template)
        every { templateInstanceUseCases.findById(template.id, resource.id) } returns Optional.empty()

        shouldThrow<TemplateInstanceNotFound> { service.create(command) }

        verify(exactly = 1) { resourceRepository.findById(resource.id) }
        verify(exactly = 1) { templateUseCase.findById(template.id) }
        verify(exactly = 1) { templateInstanceUseCases.findById(template.id, resource.id) }
    }
}
