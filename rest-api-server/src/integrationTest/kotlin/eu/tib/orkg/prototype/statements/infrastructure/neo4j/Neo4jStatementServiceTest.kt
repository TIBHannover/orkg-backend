package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class Neo4jStatementServiceTest : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var service: StatementUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.removeAll()
        predicateService.removeAll()
        service.removeAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(service.findAll(tempPageable)).hasSize(0)
    }

    @Test
    @DisplayName("shouldCreateStatementWhenAllResourcesExist")
    fun shouldCreateStatementWhenAllResourcesExist() {
        val subjectId = resourceService.create("subject").id
        val predicateId = predicateService.create("predicate").id
        val objectId = resourceService.create("object").id

        val id = service.create(
            subjectId,
            predicateId,
            objectId
        )
        val statement = service.findById(id)

        assertThat(statement).isPresent
        assertThat(statement.get()).isNotNull
        assertThat((statement.get().subject as Resource).id).isEqualTo(subjectId)
        assertThat(statement.get().predicate.id).isEqualTo(predicateId)
        assertThat((statement.get().`object` as Resource).id).isEqualTo(objectId)
    }

    @Test
    @DisplayName("should find all created statements")
    fun shouldFindAllCreatedStatements() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id
        val r2 = resourceService.create("two").id
        val r3 = resourceService.create("three").id
        val p1 = predicateService.create("greater than").id
        val p2 = predicateService.create("less than").id

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        val statements = service.findAll(pagination)

        assertThat(statements).hasSize(3)
    }

    @Test
    @DisplayName("should find statement by ID")
    fun shouldFindStatementByID() {
        val r1 = resourceService.create("one").id
        val r2 = resourceService.create("two").id
        val r3 = resourceService.create("three").id
        val p1 = predicateService.create("greater than").id
        val p2 = predicateService.create("less than").id

        val statement = service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        val result = service.findById(statement)

        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo(statement)
        assertThat((result.get().subject as Resource).id).isEqualTo(r1)
        assertThat(result.get().predicate.id).isEqualTo(p1)
        assertThat((result.get().`object` as Resource).id).isEqualTo(r2)
    }

    @Test
    @DisplayName("should find statements by subject")
    fun shouldFindStatementsBySubject() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id
        val r2 = resourceService.create("two").id
        val r3 = resourceService.create("three").id
        val p1 = predicateService.create("greater than").id
        val p2 = predicateService.create("less than").id

        service.create(r1, p2, r2)
        service.create(r1, p2, r3)
        service.create(r3, p1, r1)

        assertThat(service.findAll(pagination)).hasSize(3)

        assertThat(service.findAllBySubject(r1, pagination)).hasSize(2)
        assertThat(service.findAllBySubject(r2, pagination)).hasSize(0)
        assertThat(service.findAllBySubject(r3, pagination)).hasSize(1)
    }

    @Test
    @DisplayName("should find statements by predicate")
    fun shouldFindStatementsByPredicate() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id
        val r2 = resourceService.create("two").id
        val r3 = resourceService.create("three").id
        val p1 = predicateService.create("greater than").id
        val p2 = predicateService.create("less than").id

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll(pagination)).hasSize(3)

        assertThat(service.findAllByPredicate(p1, pagination)).hasSize(2)
        assertThat(service.findAllByPredicate(p2, pagination)).hasSize(1)
    }

    @Test
    @DisplayName("does not create duplicate statements")
    fun doesNotCreateDuplicateStatements() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id
        val r2 = resourceService.create("two").id
        val p1 = predicateService.create("less than").id

        val s1 = service.create(r1, p1, r2)
        val s2 = service.create(r1, p1, r2)

        assertThat(s2).isEqualTo(s1)
        assertThat(service.findAll(pagination)).hasSize(1)
    }
}
