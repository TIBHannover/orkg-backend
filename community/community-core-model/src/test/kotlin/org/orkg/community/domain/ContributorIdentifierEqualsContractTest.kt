package org.orkg.community.domain

import com.redfin.contractual.EqualsContract
import org.orkg.common.ResearchGateId
import org.orkg.community.testing.fixtures.createContributorIdentifier
import java.util.function.Supplier

internal class ContributorIdentifierEqualsContractTest : EqualsContract<ContributorIdentifier> {
    private val equalID = ResearchGateId.of("SOME_ID")
    private val differentID = ResearchGateId.of("another_id")

    override fun getInstance(): ContributorIdentifier = createContributorIdentifier(value = equalID)

    override fun getEqualInstanceSupplier(): Supplier<ContributorIdentifier> = Supplier { createContributorIdentifier(value = equalID) }

    override fun getNonEqualInstance(): ContributorIdentifier = createContributorIdentifier(value = differentID)
}
