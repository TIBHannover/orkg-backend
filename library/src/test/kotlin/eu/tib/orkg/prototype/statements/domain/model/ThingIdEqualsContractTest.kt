package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import java.util.function.Supplier

internal class ThingIdEqualsContractTest : EqualsContract<ThingId> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    override fun getInstance(): ThingId = ThingId.of(equalID)

    override fun getEqualInstanceSupplier(): Supplier<ThingId> = Supplier { ThingId.of(equalID) }

    override fun getNonEqualInstance(): ThingId = ThingId.of(differentID)
}
