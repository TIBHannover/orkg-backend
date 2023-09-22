package org.orkg.statements.testing

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import java.time.OffsetDateTime

fun createStatement(
    id: StatementId = StatementId(1),
    subject: Thing = createClass(),
    predicate: Predicate = createPredicate(),
    `object`: Thing = createClass(),
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: ContributorId = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
) = GeneralStatement(id, subject, predicate, `object`, createdAt, createdBy)
