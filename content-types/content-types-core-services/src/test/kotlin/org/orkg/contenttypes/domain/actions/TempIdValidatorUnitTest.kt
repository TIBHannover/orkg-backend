package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidTempId

internal class TempIdValidatorUnitTest {
    private val tempIdValidator = TempIdValidator<List<String>?, Unit> { it }

    @Test
    fun `Given a list of temp ids, when validating, it returns success`() {
        tempIdValidator(listOf("#temp1", "#temp2", "#temp3", "#temp4"), Unit)
    }

    @Test
    fun `Given an empty list of temp ids, when validating, it returns success`() {
        tempIdValidator(emptyList(), Unit)
    }

    @Test
    fun `Given a list of temp ids, when temp id is too short, it throws an error`() {
        assertThrows<InvalidTempId> {
            tempIdValidator(listOf("#"), Unit)
        }
    }

    @Test
    fun `Given a list of temp ids, when null, it returns success`() {
        tempIdValidator(null, Unit)
    }

    @Test
    fun `Given a list of temp ids, when temp id is duplicate, it throws an error`() {
        val ids = listOf("#duplicate", "#duplicate")

        val result = assertThrows<DuplicateTempIds> {
            tempIdValidator(ids, Unit)
        }

        result.duplicates shouldBe mapOf("#duplicate" to 2)
    }
}
