package org.orkg.graph.domain

import com.redfin.contractual.EqualsContract
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Supplier

internal class PredicateEqualsContractTest : EqualsContract<Predicate> {
    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    private val timeStamp = "2023-11-30T09:25:14.049085776+01:00"
    private val contributorUUID = UUID.randomUUID()

    override fun getInstance(): Predicate = Predicate(
        id = ThingId(equalID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
    )

    override fun getEqualInstanceSupplier(): Supplier<Predicate> = Supplier {
        Predicate(
            id = ThingId(equalID),
            label = "some label",
            createdAt = OffsetDateTime.parse(timeStamp),
            createdBy = ContributorId(contributorUUID),
        )
    }

    override fun getNonEqualInstance(): Predicate = Predicate(
        id = ThingId(differentID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
    )
}
