package org.orkg.contenttypes.domain.actions

import io.mockk.mockk
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

abstract class AbstractTemplatePropertyUpdaterUnitTest : MockkBaseTest {
    protected val statementService: StatementUseCases = mockk()
    protected val resourceService: ResourceUseCases = mockk()
    protected val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    protected val abstractTemplatePropertyUpdater = AbstractTemplatePropertyUpdater(statementService, resourceService, singleStatementPropertyUpdater)
}
