package org.orkg.contenttypes.domain.actions.papers

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.IdentifierValidator
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Classes

internal class PaperIdentifierCreateValidatorUnitTest : MockkBaseTest {
    private val identifierValidator: IdentifierValidator = mockk()

    private val paperIdentifierCreateValidator = PaperIdentifierCreateValidator(identifierValidator)

    @Test
    fun `Given a paper create command, when validating its identifiers, it returns success`() {
        val command = createPaperCommand()
        val state = CreatePaperState()

        every {
            identifierValidator.validate(
                identifiers = command.identifiers,
                `class` = Classes.paper,
                subjectId = null,
                exceptionFactory = PaperAlreadyExists::withIdentifier
            )
        } just runs

        paperIdentifierCreateValidator(command, state)

        verify(exactly = 1) {
            identifierValidator.validate(
                identifiers = command.identifiers,
                `class` = Classes.paper,
                subjectId = null,
                exceptionFactory = PaperAlreadyExists::withIdentifier
            )
        }
    }
}
