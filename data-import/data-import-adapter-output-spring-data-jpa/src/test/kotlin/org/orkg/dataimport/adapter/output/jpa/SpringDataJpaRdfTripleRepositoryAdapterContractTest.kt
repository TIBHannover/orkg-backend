package org.orkg.dataimport.adapter.output.jpa

import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaConfiguration
import org.orkg.dataimport.adapter.output.jpa.configuration.DataImportJpaTestConfiguration
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.dataimport.output.testing.fixtures.RdfTripleRepositoryContracts
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaRdfTripleRepositoryAdapter::class,
        SpringDataJpaRdfResourceRepositoryAdapter::class,
        SpringDataJpaRdfClassRepositoryAdapter::class,
        SpringDataJpaRdfPredicateRepositoryAdapter::class,
        DataImportJpaConfiguration::class,
        DataImportJpaTestConfiguration::class,
    ],
    initializers = [PostgresContainerInitializer::class],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaRdfTripleRepositoryAdapterContractTest : RdfTripleRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaRdfTripleRepositoryAdapter

    @Autowired
    private lateinit var resourceAdapter: SpringDataJpaRdfResourceRepositoryAdapter

    @Autowired
    private lateinit var classAdapter: SpringDataJpaRdfClassRepositoryAdapter

    @Autowired
    private lateinit var predicateAdapter: SpringDataJpaRdfPredicateRepositoryAdapter

    override val repository: RdfTripleRepository
        get() = adapter

    override val resourceRepository: RdfResourceRepository
        get() = resourceAdapter

    override val classRepository: RdfClassRepository
        get() = classAdapter

    override val predicateRepository: RdfPredicateRepository
        get() = predicateAdapter
}
