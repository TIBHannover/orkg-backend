package org.orkg.discussions.adapter.output.jpa

import org.orkg.auth.adapter.output.jpa.JpaUserAdapter
import org.orkg.auth.adapter.output.jpa.configuration.AuthJpaConfiguration
import org.orkg.auth.output.UserRepository
import org.orkg.discussions.adapter.output.jpa.configuration.DiscussionsJpaConfiguration
import org.orkg.discussions.output.DiscussionCommentRepository
import org.orkg.discussions.output.DiscussionCommentRepositoryContractTest
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

@DataJpaTest
@ContextConfiguration(
    classes = [
        SpringJpaPostgresDiscussionCommentAdapter::class,
        AuthJpaConfiguration::class,
        DiscussionsJpaConfiguration::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SpringJpaPostgresDiscussionCommentAdapterContractTests : DiscussionCommentRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresDiscussionCommentAdapter

    @Autowired
    private lateinit var userAdapter: JpaUserAdapter

    override val repository: DiscussionCommentRepository
        get() = adapter

    // TODO: Replace with adapter
    override val userRepository: UserRepository
        get() = userAdapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
        userRepository.deleteAll()
    }
}
