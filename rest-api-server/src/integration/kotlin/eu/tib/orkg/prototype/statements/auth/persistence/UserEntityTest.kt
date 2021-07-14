package eu.tib.orkg.prototype.statements.auth.persistence

import eu.tib.orkg.prototype.TestContainersJpaTest
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import java.util.UUID
import javax.persistence.PersistenceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.hibernate.id.IdentifierGenerationException
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.TestPropertySource

@TestContainersJpaTest
@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.javax.persistence.validation.mode=none"
    ]
)
class UserEntityTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun testThatUniqueConstraintForIdExistsInDatabase() {
        // given a user with a given ID
        val withGivenId = UUID.fromString("5b7bec6e-d0d4-42f3-974b-3ecce9a8e44e")
        val user = createValidUserEntity(withGivenId, "one@example.org")
        entityManager.persistAndFlush(user)
        entityManager.clear()

        // when a user with the same ID is saved
        val newUser = createValidUserEntity(withGivenId, "two@example.org")
        val thrown = catchThrowable { entityManager.persistAndFlush(newUser) }

        // then
        assertThat(thrown)
            .isInstanceOf(PersistenceException::class.java)
            .hasRootCauseExactlyInstanceOf(PSQLException::class.java)
            .hasStackTraceContaining("ERROR: duplicate key value violates unique constraint \"user_pk\"")
    }

    @Test
    fun testThatNotNullConstraintForIdExistsInDatabase() {
        // given a user with a null id
        val user = createValidUserEntity(id = null)

        val thrown = catchThrowable { entityManager.persistAndFlush(user) }

        // then
        assertThat(thrown)
            .isInstanceOf(PersistenceException::class.java)
            .hasRootCauseExactlyInstanceOf(IdentifierGenerationException::class.java)
            .hasMessageContaining("ids for this class must be manually assigned before calling save():")
    }

    private fun createValidUserEntity(id: UUID? = UUID.randomUUID(), email: String = "user@example.org") =
        UserEntity().apply {
            this.id = id
            this.email = email
            password = "\$2a\$10\$iwPkZ6bLr35Q9xJ5bzuGIuFdzZK8WE.xiRUMTMzPEmtmioOh8ybQe" // password = "test"
        }
}
