package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit.jupiter.*
import org.springframework.transaction.annotation.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
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
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        val statements = service.findAll()

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
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll()).hasSize(4)

        assertThat(service.findAllBySubject(r1)).hasSize(3)
        assertThat(service.findAllBySubject(r2)).hasSize(0)
        assertThat(service.findAllBySubject(r3)).hasSize(1)
    }

    @Test
    @DisplayName("should find statements by predicate")
    fun shouldFindStatementsByPredicate() {
        val r1 = resourceService.create("one").id!!
        val r2 = resourceService.create("two").id!!
        val r3 = resourceService.create("three").id!!
        val p1 = predicateService.create("greater than").id!!
        val p2 = predicateService.create("less than").id!!

        service.create(r1, p1, r2)
        service.create(r1, p1, r3)
        service.create(r1, p1, r3)
        service.create(r3, p2, r1)

        assertThat(service.findAll()).hasSize(4)

        assertThat(service.findAllByPredicate(p1)).hasSize(3)
        assertThat(service.findAllByPredicate(p2)).hasSize(1)
    }
}
