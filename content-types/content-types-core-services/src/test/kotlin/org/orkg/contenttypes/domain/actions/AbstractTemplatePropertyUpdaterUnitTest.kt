package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

abstract class AbstractTemplatePropertyUpdaterUnitTest {
    protected val statementService: StatementUseCases = mockk()
    protected val resourceService: ResourceUseCases = mockk()
    protected val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    protected val abstractTemplatePropertyUpdater = AbstractTemplatePropertyUpdater(statementService, resourceService, singleStatementPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService, singleStatementPropertyUpdater)
    }
}
