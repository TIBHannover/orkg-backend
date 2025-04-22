package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.contenttypes.input.testing.fixtures.updateTableCommand
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class TableUpdateValidationCacheInitializerUnitTest {
    private val tableUpdateValidationCacheInitializer = TableUpdateValidationCacheInitializer()

    @Test
    fun `Given a table update command, when initializing the validation cache, it returns success`() {
        val command = updateTableCommand()

        val r1 = createResource(ThingId("R1"))
        val r2 = createResource(ThingId("R2"))

        val p1 = createPredicate(ThingId("P1"))
        val p2 = createPredicate(ThingId("P2"))

        val c1 = createClass(ThingId("C1"))
        val c2 = createClass(ThingId("C2"))

        val l1 = createLiteral(ThingId("L1"))

        val statements = listOf(
            createStatement(subject = r1, predicate = p1, `object` = c1),
            createStatement(subject = r2, predicate = p2, `object` = l1),
            createStatement(subject = c1, predicate = p2, `object` = c2),
        ).groupBy { it.subject.id }
        val state = UpdateTableState().copy(
            statements = statements,
            validationCache = mapOf(
                "#temp1" from command,
                "#temp2" from command,
                "#temp3" from command
            )
        )
        val expected = state.validationCache + mapOf(
            "R1" to Either.right(r1),
            "R2" to Either.right(r2),
            "P1" to Either.right(p1),
            "P2" to Either.right(p2),
            "C1" to Either.right(c1),
            "C2" to Either.right(c2),
            "L1" to Either.right(l1),
        )

        val result = tableUpdateValidationCacheInitializer(command, state)

        result.asClue {
            it.table shouldBe state.table
            it.validationCache shouldBe expected
            it.tempIdToThingId shouldBe state.tempIdToThingId
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }
    }
}
