package org.orkg.community.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.configuration.AuthJpaConfiguration
import org.orkg.auth.output.UserRepository
import org.orkg.community.adapter.output.jpa.configuration.CommunityJpaConfiguration
import org.orkg.community.output.ObservatoryFilterRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.community.testing.fixtures.ObservatoryFilterRepositoryContractTests
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringJpaPostgresObservatoryFilterAdapter::class,
        CommunityJpaConfiguration::class,
        AuthJpaConfiguration::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PostgresObservatoryFilterContractTests : ObservatoryFilterRepositoryContractTests {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresObservatoryFilterAdapter

    @Autowired
    private lateinit var observatoryAdapter: ObservatoryRepository

    @Autowired
    private lateinit var organizationAdapter: OrganizationRepository

    @Autowired
    @Qualifier("jpaUserAdapter")
    private lateinit var userAdapter: UserRepository

    override val repository: ObservatoryFilterRepository
        get() = adapter

    override val observatoryRepository: ObservatoryRepository
        get() = observatoryAdapter

    override val organizationRepository: OrganizationRepository
        get() = organizationAdapter

    override val userRepository: UserRepository
        get() = userAdapter

    override fun cleanUpAfterEach() {
        adapter.deleteAll()
        observatoryAdapter.deleteAll()
        organizationAdapter.deleteAll()
        userAdapter.deleteAll()
    }
}
