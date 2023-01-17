package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import java.util.function.Supplier

internal class LiteralIdEqualsContractTest : EqualsContract<LiteralId> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    override fun getInstance(): LiteralId = LiteralId(equalID)

    override fun getEqualInstanceSupplier(): Supplier<LiteralId> = Supplier { LiteralId(equalID) }

    override fun getNonEqualInstance(): LiteralId = LiteralId(differentID)
}
