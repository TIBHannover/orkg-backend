package org.orkg.discussions.adapter.output.jpa

import org.orkg.discussions.adapter.output.jpa.configuration.DiscussionsJpaConfiguration
import org.orkg.discussions.output.DiscussionCommentRepository
import org.orkg.discussions.output.DiscussionCommentRepositoryContractTest
import org.orkg.testing.configuration.FixedClockConfig
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
        DiscussionsJpaConfiguration::class,
        FixedClockConfig::class
    ],
    initializers = [PostgresContainerInitializer::class]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class SpringJpaPostgresDiscussionCommentAdapterContractTests : DiscussionCommentRepositoryContractTest() {

    @Autowired
    private lateinit var adapter: SpringJpaPostgresDiscussionCommentAdapter

    override val repository: DiscussionCommentRepository
        get() = adapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
    }
}
