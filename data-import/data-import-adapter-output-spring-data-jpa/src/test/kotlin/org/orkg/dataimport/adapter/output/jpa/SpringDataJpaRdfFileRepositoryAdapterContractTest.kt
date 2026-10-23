package org.orkg.dataimport.adapter.output.jpa

import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaConfiguration
import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaTestConfiguration
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.dataimport.output.testing.fixtures.RdfFileRepositoryContracts
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaRdfFileRepositoryAdapter::class,
        DataImportJpaConfiguration::class,
        DataImportJpaTestConfiguration::class,
    ],
    initializers = [PostgresContainerInitializer::class],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaRdfFileRepositoryAdapterContractTest : RdfFileRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaRdfFileRepositoryAdapter

    override val repository: RdfFileRepository
        get() = adapter
}
