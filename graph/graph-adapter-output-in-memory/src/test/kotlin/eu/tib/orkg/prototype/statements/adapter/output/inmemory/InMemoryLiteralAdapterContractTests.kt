package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.spi.literalRepositoryContract
import io.kotest.core.spec.style.DescribeSpec

internal class InMemoryLiteralAdapterContractTests : DescribeSpec({
    include(literalRepositoryContract(InMemoryLiteralRepository()))
})
