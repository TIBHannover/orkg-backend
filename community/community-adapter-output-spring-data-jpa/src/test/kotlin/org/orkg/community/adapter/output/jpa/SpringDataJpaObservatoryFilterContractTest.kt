package org.orkg.community.adapter.output.jpa

import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaTestConfiguration
import org.orkg.community.output.ObservatoryFilterRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.ObservatoryFilterRepositoryContracts
import org.orkg.eventbus.EventBus
import org.orkg.eventbus.ReallySimpleEventBus
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringDataJpaObservatoryFilterAdapter::class,
        CommunityJpaConfiguration::class,
        CommunityJpaTestConfiguration::class,
        ReallySimpleEventBus::class,
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringDataJpaObservatoryFilterContractTest : ObservatoryFilterRepositoryContracts {
    @Autowired
    private lateinit var adapter: SpringDataJpaObservatoryFilterAdapter

    @Autowired
    private lateinit var observatoryAdapter: ObservatoryRepository

    @Autowired
    private lateinit var organizationAdapter: OrganizationRepository

    @Autowired
    private lateinit var reallySimpleEventBus: ReallySimpleEventBus

    override val repository: ObservatoryFilterRepository
        get() = adapter

    override val observatoryRepository: ObservatoryRepository
        get() = observatoryAdapter

    override val organizationRepository: OrganizationRepository
        get() = organizationAdapter

    override val eventBus: EventBus
        get() = reallySimpleEventBus

    override fun cleanUpAfterEach() {
        adapter.deleteAll()
        observatoryAdapter.deleteAll()
        organizationAdapter.deleteAll()
    }
}
