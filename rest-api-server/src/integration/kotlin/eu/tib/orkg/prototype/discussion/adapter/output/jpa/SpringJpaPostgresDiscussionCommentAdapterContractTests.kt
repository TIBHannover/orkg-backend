package eu.tib.orkg.prototype.discussion.adapter.output.jpa

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.JpaUserAdapter
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.discussions.adapter.output.jpa.SpringJpaPostgresDiscussionCommentAdapter
import eu.tib.orkg.prototype.discussions.spi.DiscussionCommentRepository
import eu.tib.orkg.prototype.discussions.spi.DiscussionCommentRepositoryContractTest
import eu.tib.orkg.prototype.testing.PostgresTestContainersBaseTest
import org.springframework.beans.factory.annotation.Autowired

class SpringJpaPostgresDiscussionCommentAdapterContractTests : PostgresTestContainersBaseTest(),
    DiscussionCommentRepositoryContractTest {

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
