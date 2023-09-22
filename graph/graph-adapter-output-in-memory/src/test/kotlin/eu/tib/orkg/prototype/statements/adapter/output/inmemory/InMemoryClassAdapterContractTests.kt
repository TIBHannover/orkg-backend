package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.spi.classRepositoryContract
import io.kotest.core.spec.style.DescribeSpec

internal class InMemoryClassAdapterContractTests : DescribeSpec({
    include(
        classRepositoryContract(
            InMemoryClassRepository(),
            InMemoryStatementRepository(),
            InMemoryLiteralRepository(),
            InMemoryPredicateRepository()
        )
    )
})
