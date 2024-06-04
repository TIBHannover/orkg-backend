package org.orkg.graph.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@Neo4jContainerIntegrationTest
class StatementServiceIntegrationTest {

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
        val subjectId = resourceService.createResource(label = "subject")
        val predicateId = predicateService.createPredicate(label = "predicate")
        val objectId = resourceService.createResource(label = "object")

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
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val p1 = predicateService.createPredicate(label = "greater than")
        val p2 = predicateService.createPredicate(label = "less than")

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        val statements = service.findAll(pagination)

        assertThat(statements).hasSize(3)
    }

    @Test
    @DisplayName("should find statement by ID")
    fun shouldFindStatementByID() {
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val p1 = predicateService.createPredicate(label = "greater than")
        val p2 = predicateService.createPredicate(label = "less than")

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
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val p1 = predicateService.createPredicate(label = "greater than")
        val p2 = predicateService.createPredicate(label = "less than")

        service.create(r1, p2, r2)
        service.create(r1, p2, r3)
        service.create(r3, p1, r1)

        assertThat(service.findAll(pagination)).hasSize(3)

        assertThat(service.findAll(subjectId = r1, pageable = pagination)).hasSize(2)
        assertThat(service.findAll(subjectId = r2, pageable = pagination)).hasSize(0)
        assertThat(service.findAll(subjectId = r3, pageable = pagination)).hasSize(1)
    }

    @Test
    @DisplayName("should find statements by predicate")
    fun shouldFindStatementsByPredicate() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val r3 = resourceService.createResource(label = "three")
        val p1 = predicateService.createPredicate(label = "greater than")
        val p2 = predicateService.createPredicate(label = "less than")

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll(pagination)).hasSize(3)

        assertThat(service.findAll(predicateId = p1, pageable = pagination)).hasSize(2)
        assertThat(service.findAll(predicateId = p2, pageable = pagination)).hasSize(1)
    }

    @Test
    @DisplayName("does not create duplicate statements")
    fun doesNotCreateDuplicateStatements() {
        val pagination = PageRequest.of(0, 10)
        val r1 = resourceService.createResource(label = "one")
        val r2 = resourceService.createResource(label = "two")
        val p1 = predicateService.createPredicate(label = "less than")

        val s1 = service.create(r1, p1, r2)
        val s2 = service.create(r1, p1, r2)

        assertThat(s2).isEqualTo(s1)
        assertThat(service.findAll(pagination)).hasSize(1)
    }
}
