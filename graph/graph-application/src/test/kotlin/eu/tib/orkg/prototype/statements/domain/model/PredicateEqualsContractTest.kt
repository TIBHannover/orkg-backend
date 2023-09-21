package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Supplier

internal class PredicateEqualsContractTest : EqualsContract<Predicate> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    private val timeStamp = OffsetDateTime.now().toString()
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