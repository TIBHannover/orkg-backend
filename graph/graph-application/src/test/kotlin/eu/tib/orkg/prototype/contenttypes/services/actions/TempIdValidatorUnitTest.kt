package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.DuplicateTempIds
import eu.tib.orkg.prototype.contenttypes.application.InvalidTempId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TempIdValidatorUnitTest {
    private val tempIdValidator = object : TempIdValidator() {}

    @Test
    fun `Given a list of temp ids, when validating, it returns success`() {
        tempIdValidator.validate(listOf("#temp1", "#temp2", "#temp3", "#temp4"))
    }

    @Test
    fun `Given an empty list of temp ids, when validating, it returns success`() {
        tempIdValidator.validate(emptyList())
    }

    @Test
    fun `Given a list of temp ids, when temp id is too short, it throws an error`() {
        assertThrows<InvalidTempId> {
            tempIdValidator.validate(listOf("#"))
        }
    }

    @Test
    fun `Given a list of temp ids, when temp id is duplicate, it throws an error`() {
        val ids = listOf("#duplicate", "#duplicate")

        val result = assertThrows<DuplicateTempIds> {
            tempIdValidator.validate(ids)
        }

        result.duplicates shouldBe mapOf("#duplicate" to 2)
    }
}
