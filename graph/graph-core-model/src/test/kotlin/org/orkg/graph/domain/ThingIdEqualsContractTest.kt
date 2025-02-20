package org.orkg.graph.domain

import com.redfin.contractual.EqualsContract
import org.orkg.common.ThingId
import java.util.function.Supplier

internal class ThingIdEqualsContractTest : EqualsContract<ThingId> {
    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    override fun getInstance(): ThingId = ThingId(equalID)

    override fun getEqualInstanceSupplier(): Supplier<ThingId> = Supplier { ThingId(equalID) }

    override fun getNonEqualInstance(): ThingId = ThingId(differentID)
}
