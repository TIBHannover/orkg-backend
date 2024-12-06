package org.orkg.graph.adapter.output.inmemory

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.testing.fixtures.statementRepositoryContract

internal class InMemoryStatementAdapterContractTest : DescribeSpec({
    val inMemoryGraph = InMemoryGraph()
    include(
        statementRepositoryContract(
            InMemoryStatementRepository(inMemoryGraph),
            InMemoryClassRepository(inMemoryGraph),
            InMemoryLiteralRepository(inMemoryGraph),
            InMemoryResourceRepository(inMemoryGraph),
            InMemoryPredicateRepository(inMemoryGraph)
        )
    )
})
