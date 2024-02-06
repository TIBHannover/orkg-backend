package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.statementRepositoryContract

internal class InMemoryStatementAdapterContractTests : DescribeSpec({
    val inMemoryGraph = InMemoryGraph()
    include(
        statementRepositoryContract(
            InMemoryStatementRepository(),
            InMemoryClassRepository(inMemoryGraph),
            InMemoryLiteralRepository(inMemoryGraph),
            InMemoryResourceRepository(inMemoryGraph),
            InMemoryPredicateRepository()
        )
    )
})
