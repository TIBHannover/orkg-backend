package org.orkg.contenttypes.domain.actions.tables

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.input.testing.fixtures.createTableCommand

internal class TableTempIdCreateValidatorUnitTest {
    private val tableTempIdCreateValidator = TableTempIdCreateValidator()

    @Test
    fun `Given a table create command, when validating its temp ids, it returns success`() {
        val command = createTableCommand()
        val state = CreateTableState()

        val result = tableTempIdCreateValidator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.tempIds shouldBe setOf("#temp1", "#temp2", "#temp3", "#temp4", "#temp5", "#temp6", "#temp7")
            it.validatedIds shouldBe state.validatedIds
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }
    }

    @Test
    fun `Given a table create command, when it has no new thing definitions, it returns success`() {
        val command = createTableCommand().copy(
            resources = emptyMap(),
            predicates = emptyMap(),
            literals = emptyMap(),
            lists = emptyMap(),
            classes = emptyMap()
        )
        val state = CreateTableState()

        val result = tableTempIdCreateValidator(command, state)

        result.asClue {
            it.tableId shouldBe state.tableId
            it.tempIds shouldBe emptySet()
            it.validatedIds shouldBe state.validatedIds
            it.tempIdToThing shouldBe state.tempIdToThing
            it.columns shouldBe state.columns
            it.rows shouldBe state.rows
        }
    }
}
