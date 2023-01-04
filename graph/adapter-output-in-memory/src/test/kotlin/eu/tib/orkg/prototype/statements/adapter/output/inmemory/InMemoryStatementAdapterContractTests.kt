package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.spi.statementRepositoryContract
import io.kotest.core.spec.style.DescribeSpec

internal class InMemoryStatementAdapterContractTests : DescribeSpec({
    include(statementRepositoryContract(InMemoryStatementRepository()))
})
