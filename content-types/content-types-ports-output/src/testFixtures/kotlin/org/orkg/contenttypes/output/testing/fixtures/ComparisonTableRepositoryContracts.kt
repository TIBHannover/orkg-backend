package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.testing.fixtures.withContentTypeMappings
import org.orkg.contenttypes.output.ComparisonTableRepository
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

interface ComparisonTableRepositoryContracts {
    val repository: ComparisonTableRepository

    @Test
    fun `Saving a comparison table, saves and loads all properties correctly`() {
        val expected: ComparisonTable = fabricator.random()
        repository.save(expected)

        val actual = repository.findByComparisonId(expected.comparisonId).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.comparisonId shouldBe expected.comparisonId
            it.selectedPaths shouldBe expected.selectedPaths
            it.titles shouldBe expected.titles
            it.subtitles shouldBe expected.subtitles
            it.values shouldBe expected.values
        }
    }

    @Test
    fun `Counting comparison tables, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<ComparisonTable>())
        }

        repository.count() shouldBe 3
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
