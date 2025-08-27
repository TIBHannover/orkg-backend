package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.dataimport.output.testing.fixtures.PaperCSVRecordRepositoryContracts

internal class InMemoryPaperCSVRecordRepositoryAdapterContractTest : PaperCSVRecordRepositoryContracts {
    override val repository: PaperCSVRecordRepository = InMemoryPaperCSVRecordRepositoryAdapter()

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
