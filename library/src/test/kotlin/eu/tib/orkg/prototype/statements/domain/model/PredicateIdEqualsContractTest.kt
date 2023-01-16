package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import java.util.function.Supplier

internal class PredicateIdEqualsContractTest : EqualsContract<PredicateId> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    override fun getInstance(): PredicateId = PredicateId(equalID)

    override fun getEqualInstanceSupplier(): Supplier<PredicateId> = Supplier { PredicateId(equalID) }

    override fun getNonEqualInstance(): PredicateId = PredicateId(differentID)
}
