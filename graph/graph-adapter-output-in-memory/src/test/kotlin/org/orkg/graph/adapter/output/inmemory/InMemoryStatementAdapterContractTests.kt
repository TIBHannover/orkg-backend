package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.statementRepositoryContract

internal class InMemoryStatementAdapterContractTests : DescribeSpec({
    include(
        statementRepositoryContract(
            InMemoryStatementRepository(),
            InMemoryClassRepository(),
            InMemoryLiteralRepository(),
            InMemoryResourceRepository(),
            InMemoryPredicateRepository()
        )
    )
})
