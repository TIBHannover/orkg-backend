package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

interface PredicateRepositoryContractTest {
    val repository: PredicateRepository

    val statementRepository: StatementRepository

    val resourceRepository: ResourceRepository

    @Test
    fun `successfully delete a predicate`() {
        val predicate = createPredicate()
        repository.save(predicate)
        repository.findByPredicateId(predicate.id).isPresent shouldBe true
        repository.deleteByPredicateId(predicate.id!!)
        repository.findByPredicateId(predicate.id).isPresent shouldBe false
    }

    @Test
    fun `given a predicate is used in a statement (regardless of position), count its usage, should be 0`() {
        val predicate = createPredicate()
        repository.save(predicate)

        val result = repository.usageCount(predicate.id!!)
        result shouldBe 0
    }

    @Test
    fun `given a predicate is used in a statement, count its usage, should be 1`() {
        val classes = setOf(ThingId("Thing"))
        val subject = createResource().copy(
            id = ResourceId("R1234"),
            classes = classes,
            featured = null,
            unlisted = null
        )
        resourceRepository.save(subject)
        val `object` = createResource().copy(
            id = ResourceId("R2345"),
            classes = classes,
            featured = null,
            unlisted = null
        )
        resourceRepository.save(`object`)
        val predicate = createPredicate()
        repository.save(predicate)
        val statement = createStatement(subject, predicate, `object`)
        statementRepository.save(statement)

        val result = repository.usageCount(predicate.id!!)
        result shouldBe 1
    }

    @Test
    fun `given a predicate is used as a subject, count its usage, should be 1`() {
        val classes = setOf(ThingId("Thing"))
        val subjectPredicate = createPredicate().copy(
            id = PredicateId(1)
        )
        repository.save(subjectPredicate)
        val `object` = createResource().copy(
            id = ResourceId("R2345"),
            classes = classes,
            featured = null,
            unlisted = null
        )
        resourceRepository.save(`object`)
        val otherPredicate = createPredicate().copy(
            id = PredicateId(2)
        )
        repository.save(otherPredicate)
        val statement = createStatement(subjectPredicate, otherPredicate, `object`)
        statementRepository.save(statement)

        val result = repository.usageCount(subjectPredicate.id!!)
        result shouldBe 1
    }

    @Test
    fun `given a predicate is as an object, count its usage, should be 1`() {
        val classes = setOf(ThingId("Thing"))
        val subject = createResource().copy(
            id = ResourceId("R1234"),
            classes = classes,
            featured = null,
            unlisted = null
        )
        resourceRepository.save(subject)
        val objectPredicate = createPredicate().copy(
            id = PredicateId(1)
        )
        repository.save(objectPredicate)
        val otherPredicate = createPredicate().copy(
            id = PredicateId(2)
        )
        repository.save(otherPredicate)
        val statement = createStatement(subject, otherPredicate, objectPredicate)
        statementRepository.save(statement)

        val result = repository.usageCount(objectPredicate.id!!)
        result shouldBe 1
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
