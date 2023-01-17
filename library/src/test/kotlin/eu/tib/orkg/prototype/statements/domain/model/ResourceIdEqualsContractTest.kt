package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import java.util.function.Supplier

internal class ResourceIdEqualsContractTest : EqualsContract<ResourceId> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    override fun getInstance(): ResourceId = ResourceId(equalID)

    override fun getEqualInstanceSupplier(): Supplier<ResourceId> = Supplier { ResourceId(equalID) }

    override fun getNonEqualInstance(): ResourceId = ResourceId(differentID)
}
