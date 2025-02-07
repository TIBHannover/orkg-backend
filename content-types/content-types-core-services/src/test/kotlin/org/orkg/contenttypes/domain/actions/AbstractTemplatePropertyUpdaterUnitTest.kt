package org.orkg.contenttypes.domain.actions

import io.mockk.mockk
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

abstract class AbstractTemplatePropertyUpdaterUnitTest : MockkBaseTest {
    protected val statementService: StatementUseCases = mockk()
    protected val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    protected val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    protected val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    protected val abstractTemplatePropertyUpdater = AbstractTemplatePropertyUpdater(
        statementService,
        unsafeStatementUseCases,
        unsafeResourceUseCases,
        singleStatementPropertyUpdater
    )
}
