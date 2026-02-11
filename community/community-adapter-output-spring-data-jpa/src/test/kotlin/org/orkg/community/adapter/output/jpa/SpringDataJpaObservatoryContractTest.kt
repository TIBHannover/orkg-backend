package org.orkg.community.adapter.output.jpa

import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaTestConfiguration
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.ObservatoryRepositoryContracts
import org.orkg.eventbus.ReallySimpleEventBus
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaObservatoryAdapter::class,
        CommunityJpaConfiguration::class,
        CommunityJpaTestConfiguration::class,
        ReallySimpleEventBus::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaObservatoryContractTest : ObservatoryRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaObservatoryAdapter

    @Autowired
    private lateinit var organizationAdapter: SpringDataJpaOrganizationAdapter

    override val repository: ObservatoryRepository
        get() = adapter

    override val organizationRepository: OrganizationRepository
        get() = organizationAdapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
        organizationAdapter.deleteAll()
    }
}
