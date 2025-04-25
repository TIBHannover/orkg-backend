package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotV1
import org.orkg.contenttypes.domain.testing.fixtures.withContentTypeMappings
import org.orkg.contenttypes.output.TemplateBasedResourceSnapshotRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest

private val fabricator = Fabrikate(
    FabricatorConfig(
        collectionSizes = 12..12,
        nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
    )
        .withStandardMappings()
        .withGraphMappings()
        .withContentTypeMappings()
)

interface TemplateBasedResourceSnapshotRepositoryContracts {
    val repository: TemplateBasedResourceSnapshotRepository

    @Test
    fun `Saving a template based resource snapshot, saves and loads all properties correctly`() {
        val expected: TemplateBasedResourceSnapshotV1 = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.createdAt shouldBe expected.createdAt
            it.createdBy shouldBe expected.createdBy
            it.data shouldBe expected.data
            it.templateInstance shouldBe expected.templateInstance
            it.modelVersion shouldBe expected.modelVersion
            it.resourceId shouldBe expected.resourceId
            it.templateId shouldBe expected.templateId
            it.handle shouldBe expected.handle
        }
    }

    @Test
    fun `Finding several template based resource snapshots by resource id, returns the correct results`() {
        val resourceId = ThingId("SomeResourceId")
        val expected = fabricator.random<List<TemplateBasedResourceSnapshotV1>>()
            .map { it.copy(resourceId = resourceId) }
        expected.forEach(repository::save)

        fabricator.random<List<TemplateBasedResourceSnapshotV1>>().forEach(repository::save)

        val result = repository.findAllByResourceId(
            resourceId = resourceId,
            pageable = PageRequest.of(0, 15)
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.createdAt shouldBeLessThan b.createdAt
        }
    }

    @Test
    fun `Finding several template based resource snapshots by resource id and template id, returns the correct results`() {
        val resourceId = ThingId("SomeResourceId")
        val templateId = ThingId("SomeTemplateId")
        val expected = fabricator.random<List<TemplateBasedResourceSnapshotV1>>()
            .map { it.copy(resourceId = resourceId, templateId = templateId) }
        expected.forEach(repository::save)

        fabricator.random<List<TemplateBasedResourceSnapshotV1>>()
            .mapIndexed { index, element ->
                if (index % 2 == 0) {
                    element.copy(resourceId = resourceId)
                } else {
                    element.copy(templateId = templateId)
                }
            }
            .forEach(repository::save)

        val result = repository.findAllByResourceIdAndTemplateId(
            resourceId = resourceId,
            templateId = templateId,
            pageable = PageRequest.of(0, 15)
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.createdAt shouldBeLessThan b.createdAt
        }
    }

    @Test
    fun `Counting template based resource snapshots, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<TemplateBasedResourceSnapshotV1>())
        }

        repository.count() shouldBe 3
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
