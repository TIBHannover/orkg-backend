package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class Neo4jPredicateRepositoryTest : Neo4jTestContainersBaseTest() {
    @Autowired
    private lateinit var repository: Neo4jPredicateRepository

    @Test
    fun `fetching multiple predicates by ID`() {
        val ids = (1L..20).map(::PredicateId)
        ids.forEach {
            repository.save(Neo4jPredicate(predicateId = it))
        }

        val toLoad = setOf(PredicateId(3), PredicateId(11), PredicateId(7))
        val result = repository.findAllByPredicateIdIn(toLoad)

        assertThat(result).isNotEmpty
        assertThat(result).hasSize(3)
        assertThat(result.map(Neo4jPredicate::predicateId)).containsExactlyInAnyOrder(*(toLoad.toTypedArray()))
    }

    @Test
    @Tag("regression")
    fun `fetching multiple predicates by ID, when only a single ID is given`() {
        val ids = (1L..20).map(::PredicateId)
        ids.forEach {
            repository.save(Neo4jPredicate(predicateId = it))
        }

        val toLoad = setOf(PredicateId(7))
        val result = repository.findAllByPredicateIdIn(toLoad)

        assertThat(result).isNotEmpty
        assertThat(result).hasSize(1)
        assertThat(result.map(Neo4jPredicate::predicateId)).containsExactlyInAnyOrder(*(toLoad.toTypedArray()))
    }

    @AfterEach
    fun wipeDatabase() {
        repository.deleteAll()
    }
}
