package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.Neo4jServiceTest
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@Neo4jServiceTest
class Neo4jStatementWithResourceServiceTest {

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var service: StatementWithResourceService

    @Test
    @DisplayName("shouldCreateStatementWhenAllResourcesExist")
    fun shouldCreateStatementWhenAllResourcesExist() {
        val subjectId = resourceService.create("subject").id
        val predicateId = predicateService.create("predicate").id
        val objectId = resourceService.create("object")

        val statement = service.create(
            subjectId!!,
            predicateId!!,
            objectId.id!!
        )

        assertThat(statement.subject.id).isEqualTo(subjectId)
        assertThat(statement.predicate.id).isEqualTo(predicateId)
        assertThat(statement.`object`.id).isEqualTo(objectId.id)
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

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

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

        val statement = service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        val result = service.findById(statement.id)

        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo(statement.id)
        assertThat(result.get().subject.id).isEqualTo(r1)
        assertThat(result.get().predicate.id).isEqualTo(p1)
        assertThat(result.get().`object`.id).isEqualTo(r2)
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

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll(pagination)).hasSize(4)

        assertThat(service.findAllBySubject(r1, pagination)).hasSize(3)
        assertThat(service.findAllBySubject(r2, pagination)).hasSize(0)
        assertThat(service.findAllBySubject(r3, pagination)).hasSize(1)
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

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll(pagination)).hasSize(4)

        assertThat(service.findAllByPredicate(p1, pagination)).hasSize(3)
        assertThat(service.findAllByPredicate(p2, pagination)).hasSize(1)
    }

    @Test
    @DisplayName("should find statements by subject and predicate")
    fun shouldFindStatementsBySubjectAndPredicate() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll(pagination)).hasSize(4)

        assertThat(service.findAllBySubjectAndPredicate(r1, p1, pagination)).hasSize(3)
        assertThat(service.findAllBySubjectAndPredicate(r1, p2, pagination)).hasSize(0)
        assertThat(service.findAllBySubjectAndPredicate(r3, p1, pagination)).hasSize(0)
        assertThat(service.findAllBySubjectAndPredicate(r3, p2, pagination)).hasSize(1)
    }
}
