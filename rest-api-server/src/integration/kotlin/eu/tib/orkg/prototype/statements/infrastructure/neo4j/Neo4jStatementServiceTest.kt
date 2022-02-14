package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
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
    private lateinit var service: StatementService

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
        val objectId = resourceService.create("object")

        val statement = service.create(
            subjectId!!.value,
            predicateId!!,
            objectId.id!!.value
        )

        assertThat((statement.subject as Resource).id).isEqualTo(subjectId)
        assertThat(statement.predicate.id).isEqualTo(predicateId)
        assertThat((statement.`object` as Resource).id).isEqualTo(objectId.id)
    }

    @Test
    @DisplayName("should find all created statements")
    fun shouldFindAllCreatedStatements() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1.value, p1, r2.value)
        service.create(r1.value, p1, r3.value)
        service.create(r1.value, p1, r3.value)
        service.create(r3.value, p2, r1.value)

        val statements = service.findAll(pagination)

        assertThat(statements).hasSize(4)
    }

    @Test
    @DisplayName("should find statement by ID")
    fun shouldFindStatementByID() {
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        val statement = service.create(r1.value, p1, r2.value)
        service.create(r1.value, p1, r3.value)
        service.create(r1.value, p1, r3.value)
        service.create(r3.value, p2, r1.value)

        val result = service.findById(statement.id!!)

        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo(statement.id)
        assertThat((result.get().subject as Resource).id).isEqualTo(r1)
        assertThat(result.get().predicate.id).isEqualTo(p1)
        assertThat((result.get().`object` as Resource).id).isEqualTo(r2)
    }

    @Test
    @DisplayName("should find statements by subject")
    fun shouldFindStatementsBySubject() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1.value, p1, r2.value)
        service.create(r1.value, p1, r3.value)
        service.create(r1.value, p1, r3.value)
        service.create(r3.value, p2, r1.value)

        assertThat(service.findAll(pagination)).hasSize(4)

        assertThat(service.findAllBySubject(r1.value, pagination)).hasSize(3)
        assertThat(service.findAllBySubject(r2.value, pagination)).hasSize(0)
        assertThat(service.findAllBySubject(r3.value, pagination)).hasSize(1)
    }

    @Test
    @DisplayName("should find statements by predicate")
    fun shouldFindStatementsByPredicate() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1.value, p1, r2.value)
        service.create(r1.value, p1, r3.value)
        service.create(r1.value, p1, r3.value)
        service.create(r3.value, p2, r1.value)

        assertThat(service.findAll(pagination)).hasSize(4)

        assertThat(service.findAllByPredicate(p1, pagination)).hasSize(3)
        assertThat(service.findAllByPredicate(p2, pagination)).hasSize(1)
    }
}
