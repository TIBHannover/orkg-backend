package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.predicateRepositoryContract

internal class InMemoryPredicateAdapterContractTests : DescribeSpec({
    include(
        predicateRepositoryContract(
            InMemoryPredicateRepository(),
            InMemoryStatementRepository(),
            InMemoryLiteralRepository()
        )
    )
})
