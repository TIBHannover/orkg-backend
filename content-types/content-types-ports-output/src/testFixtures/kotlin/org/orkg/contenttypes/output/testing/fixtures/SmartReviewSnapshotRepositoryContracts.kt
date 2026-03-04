package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.SmartReviewSnapshotV1
import org.orkg.contenttypes.domain.testing.fixtures.withContentTypeMappings
import org.orkg.contenttypes.output.SmartReviewSnapshotRepository
import org.orkg.graph.testing.fixtures.withGraphMappings

private val fabricator = Fabrikate(
    FabricatorConfig(
        collectionSizes = 12..12,
        nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
    )
        .withStandardMappings()
        .withGraphMappings()
        .withContentTypeMappings()
)

interface SmartReviewSnapshotRepositoryContracts {
    val repository: SmartReviewSnapshotRepository

    @Test
    fun `Saving a smart review snapshot, it saves and loads all properties correctly`() {
        val expected: SmartReviewSnapshotV1 = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.createdAt shouldBe expected.createdAt
            it.createdBy shouldBe expected.createdBy
            it.data shouldBe expected.data
            it.subgraph shouldBe expected.subgraph
            it.modelVersion shouldBe expected.modelVersion
            it.resourceId shouldBe expected.resourceId
            it.rootId shouldBe expected.rootId
        }
    }

    @Test
    fun `Finding a smart review snapshot by resource id, returns the correct result`() {
        val expected: SmartReviewSnapshotV1 = fabricator.random()
        repository.save(expected)

        val actual = repository.findByResourceId(expected.resourceId).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.createdAt shouldBe expected.createdAt
            it.createdBy shouldBe expected.createdBy
            it.data shouldBe expected.data
            it.subgraph shouldBe expected.subgraph
            it.modelVersion shouldBe expected.modelVersion
            it.resourceId shouldBe expected.resourceId
            it.rootId shouldBe expected.rootId
        }
    }

    @Test
    fun `Counting smart review snapshots, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<SmartReviewSnapshotV1>())
        }

        repository.count() shouldBe 3
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
