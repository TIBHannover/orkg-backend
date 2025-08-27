package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.orkg.dataimport.output.testing.fixtures.TypedCSVRecordRepositoryContracts

internal class InMemoryTypedCSVRecordRepositoryAdapterContractTest : TypedCSVRecordRepositoryContracts {
    override val repository: TypedCSVRecordRepository = InMemoryTypedCSVRecordRepositoryAdapter()

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
