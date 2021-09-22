package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

interface StatementRepositoryContract {
    val repositoryUnderTest: StatementRepository

    val resourceRepository: ResourceRepository
    val predicateRepository: PredicateRepository
    val literalRepository: LiteralRepository

    @Test
    @DisplayName("saves and loads a statement with a resource in the object position")
    fun saveWithResourceObject() {
        val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
        val s = Resource(ResourceId("SUB"), "a subject", createdAt = OffsetDateTime.now(clock)).persist()
        val p = Predicate(PredicateId("PRE"), "some predicate", createdAt = OffsetDateTime.now(clock)).persist()
        val o = Resource(ResourceId("OBJ"), "some object", createdAt = OffsetDateTime.now(clock)).persist()
        val expected = statementOf(s, p, o, withClock = clock)
        repositoryUnderTest.save(expected)

        val found = repositoryUnderTest.findById(expected.id!!)
            .orElseThrow { IllegalStateException("Error with test setup! The statement should be found!") }
        assertThat(found.`object`).isInstanceOf(Resource::class.java)

        val actual = found.`object` as Resource
        assertSoftly { softly ->
            softly.assertThat(actual.label).isEqualTo("some object")
            softly.assertThat(actual.createdAt).isEqualTo(OffsetDateTime.now(clock))
        }
    }

    @Test
    @DisplayName("saves and loads a statement with a literal in the object position")
    fun saveWithLiteralObject() {
        val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
        val s = Resource(ResourceId("SUB"), "a subject", createdAt = OffsetDateTime.now(clock)).persist()
        val p = Predicate(PredicateId("PRE"), "some predicate", createdAt = OffsetDateTime.now(clock)).persist()
        val o = Literal(LiteralId("LIT"), "some literal", createdAt = OffsetDateTime.now(clock)).persist()
        val expected = statementOf(s, p, o, withClock = clock)
        repositoryUnderTest.save(expected)

        val found = repositoryUnderTest.findById(expected.id!!)
            .orElseThrow { IllegalStateException("Error with test setup! The statement should be found!") }
        assertThat(found.`object`).isInstanceOf(Literal::class.java)

        val actual = found.`object` as Literal
        assertSoftly { softly ->
            softly.assertThat(actual.label).isEqualTo("some literal")
            softly.assertThat(actual.createdAt).isEqualTo(OffsetDateTime.now(clock))
        }
    }

    private fun Resource.persist() = also { resourceRepository.save(this) }

    private fun Predicate.persist() = also { predicateRepository.save(this) }

    private fun Literal.persist() = also { literalRepository.save(this) }

    private fun statementOf(s: Resource, p: Predicate, o: Thing, withClock: Clock) =
        GeneralStatement(
            id = repositoryUnderTest.nextIdentity(),
            subject = s,
            predicate = p,
            `object` = o,
            createdAt = OffsetDateTime.now(withClock),
            createdBy = ContributorId.createUnknownContributor()
        )
}
