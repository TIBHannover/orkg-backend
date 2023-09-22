package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.spi.predicateRepositoryContract
import io.kotest.core.spec.style.DescribeSpec

internal class InMemoryPredicateAdapterContractTests : DescribeSpec({
    include(
        predicateRepositoryContract(
            InMemoryPredicateRepository(),
            InMemoryStatementRepository(),
            InMemoryLiteralRepository()
        )
    )
})
