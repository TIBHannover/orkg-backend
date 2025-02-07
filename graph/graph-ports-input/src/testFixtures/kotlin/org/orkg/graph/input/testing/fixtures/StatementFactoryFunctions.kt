package org.orkg.graph.input.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UpdateStatementUseCase

fun createStatementCommand(): CreateStatementUseCase.CreateCommand =
    CreateStatementUseCase.CreateCommand(
        id = StatementId("S123"),
        contributorId = ContributorId("cfaaec46-d5e9-49a8-8c39-4aa9651928fe"),
        subjectId = ThingId("R123"),
        predicateId = ThingId("P123"),
        objectId = ThingId("L123"),
        modifiable = true
    )

fun updateStatementCommand(): UpdateStatementUseCase.UpdateCommand =
    UpdateStatementUseCase.UpdateCommand(
        statementId = StatementId("S123"),
        subjectId = ThingId("R123"),
        predicateId = ThingId("P123"),
        objectId = ThingId("L123"),
    )
