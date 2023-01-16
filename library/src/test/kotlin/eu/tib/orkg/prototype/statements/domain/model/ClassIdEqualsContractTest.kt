package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import java.util.function.Supplier

internal class ClassIdEqualsContractTest : EqualsContract<ClassId> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    override fun getInstance(): ClassId = ClassId(equalID)

    override fun getEqualInstanceSupplier(): Supplier<ClassId> = Supplier { ClassId(equalID) }

    override fun getNonEqualInstance(): ClassId = ClassId(differentID)
}
