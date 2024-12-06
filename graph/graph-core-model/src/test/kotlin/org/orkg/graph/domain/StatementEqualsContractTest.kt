package org.orkg.graph.domain

import com.redfin.contractual.EqualsContract
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Supplier
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

internal class StatementEqualsContractTest : EqualsContract<GeneralStatement> {
    private val equalID = "SOME_ID"
    private val differentID = "Sanother_id"

    private val timeStamp = "2023-11-30T09:25:14.049085776+01:00"
    private val contributorUUID = UUID.randomUUID()

    private val subjectResource = createResource(id = ThingId("R1234"))
    private val predicate = createPredicate(id = ThingId("P5555"))
    private val objectResource = createClass(id = ThingId("9886")) // to use a different Thing

    override fun getInstance(): GeneralStatement = GeneralStatement(
        id = StatementId(equalID),
        subject = subjectResource,
        predicate = predicate,
        `object` = objectResource,
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
    )

    override fun getEqualInstanceSupplier(): Supplier<GeneralStatement> = Supplier {
        GeneralStatement(
            id = StatementId(equalID),
            subject = subjectResource.copy(),
            predicate = predicate.copy(),
            `object` = objectResource.copy(),
            createdAt = OffsetDateTime.parse(timeStamp),
            createdBy = ContributorId(contributorUUID),
        )
    }

    override fun getNonEqualInstance(): GeneralStatement = GeneralStatement(
        id = StatementId(differentID),
        subject = createResource(id = ThingId("R2345")),
        predicate = createPredicate(id = ThingId("P6666")),
        `object` = createResource(id = ThingId("R8865")),
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(UUID.randomUUID()),
    )
}
