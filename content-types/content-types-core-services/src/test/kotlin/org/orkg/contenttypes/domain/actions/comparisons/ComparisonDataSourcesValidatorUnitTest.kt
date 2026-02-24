package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonDataSource
import org.orkg.contenttypes.domain.DuplicateComparisonDataSources

internal class ComparisonDataSourcesValidatorUnitTest : MockkBaseTest {
    private val comparisonDataSourcesValidator = ComparisonDataSourcesValidator<List<ComparisonDataSource>?, Unit> { it }

    @Test
    fun `Given a list of comparison data sources, when validating, it returns success`() {
        val command = listOf(
            ComparisonDataSource(ThingId("R6541"), ComparisonDataSource.Type.THING),
            ComparisonDataSource(ThingId("R6542"), ComparisonDataSource.Type.THING),
            ComparisonDataSource(ThingId("R6541"), ComparisonDataSource.Type.ROSETTA_STONE_STATEMENT),
        )
        shouldNotThrowAny { comparisonDataSourcesValidator(command, Unit) }
    }

    @Test
    fun `Given a list of comparison data sources, when it contains duplicates, it throws an exception`() {
        val command = listOf(
            ComparisonDataSource(ThingId("R6541"), ComparisonDataSource.Type.THING),
            ComparisonDataSource(ThingId("R6541"), ComparisonDataSource.Type.THING),
            ComparisonDataSource(ThingId("R6541"), ComparisonDataSource.Type.ROSETTA_STONE_STATEMENT),
        )
        shouldThrow<DuplicateComparisonDataSources> { comparisonDataSourcesValidator(command, Unit) }.asClue {
            it.duplicates shouldBe mapOf(
                command.first() to 2
            )
        }
    }

    @Test
    fun `Given a list of comparison data sources, when null, it does nothing`() {
        shouldNotThrowAny { comparisonDataSourcesValidator(null, Unit) }
    }
}
