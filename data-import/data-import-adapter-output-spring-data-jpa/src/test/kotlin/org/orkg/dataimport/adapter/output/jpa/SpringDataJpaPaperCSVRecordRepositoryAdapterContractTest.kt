package org.orkg.dataimport.adapter.output.jpa

import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaConfiguration
import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaTestConfiguration
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.dataimport.output.testing.fixtures.PaperCSVRecordRepositoryContracts
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaPaperCSVRecordRepositoryAdapter::class,
        DataImportJpaConfiguration::class,
        DataImportJpaTestConfiguration::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaPaperCSVRecordRepositoryAdapterContractTest : PaperCSVRecordRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaPaperCSVRecordRepositoryAdapter

    override val repository: PaperCSVRecordRepository
        get() = adapter

    override fun cleanUpAfterEach() {
        adapter.deleteAll()
    }
}
