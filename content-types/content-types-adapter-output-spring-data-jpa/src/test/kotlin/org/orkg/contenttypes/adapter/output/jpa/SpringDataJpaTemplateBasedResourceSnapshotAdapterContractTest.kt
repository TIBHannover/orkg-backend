package org.orkg.contenttypes.adapter.output.jpa

import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.contenttypes.adapter.output.jpa.configuration.ContentTypesJpaConfiguration
import org.orkg.contenttypes.adapter.output.jpa.configuration.ContentTypesJpaTestConfiguration
import org.orkg.contenttypes.output.TemplateBasedResourceSnapshotRepository
import org.orkg.contenttypes.output.testing.fixtures.TemplateBasedResourceSnapshotRepositoryContracts
import org.orkg.graph.adapter.input.rest.configuration.GraphSpringConfig
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaTemplateBasedResourceSnapshotAdapter::class,
        CommonSpringConfig::class,
        GraphSpringConfig::class,
        ContentTypesJpaConfiguration::class,
        ContentTypesJpaTestConfiguration::class,
        JacksonAutoConfiguration::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaTemplateBasedResourceSnapshotAdapterContractTest : TemplateBasedResourceSnapshotRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaTemplateBasedResourceSnapshotAdapter

    override val repository: TemplateBasedResourceSnapshotRepository
        get() = adapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
