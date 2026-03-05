package org.orkg.contenttypes.adapter.output.jpa

import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.contenttypes.adapter.output.jpa.configuration.ContentTypesJpaConfiguration
import org.orkg.contenttypes.adapter.output.jpa.configuration.ContentTypesJpaTestConfiguration
import org.orkg.contenttypes.output.LiteratureListSnapshotRepository
import org.orkg.contenttypes.output.testing.fixtures.LiteratureListSnapshotRepositoryContracts
import org.orkg.graph.adapter.input.rest.configuration.GraphSpringConfig
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaLiteratureListSnapshotAdapter::class,
        CommonSpringConfig::class,
        GraphSpringConfig::class,
        ContentTypesJpaConfiguration::class,
        ContentTypesJpaTestConfiguration::class,
        JacksonAutoConfiguration::class,
    ],
    initializers = [PostgresContainerInitializer::class],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaLiteratureListSnapshotAdapterContractTest : LiteratureListSnapshotRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaLiteratureListSnapshotAdapter

    override val repository: LiteratureListSnapshotRepository
        get() = adapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
