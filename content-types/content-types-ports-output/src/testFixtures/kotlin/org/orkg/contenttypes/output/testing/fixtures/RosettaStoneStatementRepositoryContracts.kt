package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.contenttypes.domain.testing.fixtures.withRosettaStoneStatementMappings
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <
    T : RosettaStoneStatementRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    C : ClassRepository,
    L : LiteralRepository,
    S : StatementRepository
> rosettaStoneStatementRepositoryContract(
    repository: T,
    resourceRepository: R,
    predicateRepository: P,
    classRepository: C,
    literalRepository: L,
    statementRepository: S
) = describeSpec {
    beforeTest {
        repository.deleteAll()
        statementRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
        classRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            seed = 16532,
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    )
        .withCustomMappings()
        .withRosettaStoneStatementMappings()

    val saveThing: (Thing) -> Unit = {
        when (it) {
            is Class -> classRepository.save(it)
            is Literal -> literalRepository.save(it)
            is Resource -> resourceRepository.save(it)
            is Predicate -> {
                predicateRepository.save(it)
                it.description?.let { description ->
                    val descriptionPredicate = fabricator.random<Predicate>()
                        .copy(id = Predicates.description)
                    val descriptionLiteral = fabricator.random<Literal>().copy(
                        label = description,
                        datatype = Literals.XSD.STRING.prefixedUri
                    )
                    statementRepository.save(
                        fabricator.random<GeneralStatement>().copy(
                            subject = it,
                            predicate = descriptionPredicate,
                            `object` = descriptionLiteral
                        )
                    )
                }
            }
        }
    }

    fun RosettaStoneStatementVersion.requiredEntities(): Set<Thing> =
        subjects.toSet() + objects.flatten()

    fun RosettaStoneStatement.requiredEntities(): Set<Thing> =
        setOfNotNull(
            fabricator.random<Resource>().copy(id = templateId, classes = setOf(Classes.rosettaNodeShape)),
            contextId?.let { fabricator.random<Resource>().copy(id = it) },
        ) + versions.flatMap { it.requiredEntities() }

    describe("saving a rosetta stone statement") {
        it("saves and loads all properties correctly") {
            val expected: RosettaStoneStatement = fabricator.random()
            expected.requiredEntities().forEach(saveThing)
            repository.save(expected)

            val actual = repository.findByIdOrVersionId(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.contextId shouldBe expected.contextId
                it.templateId shouldBe expected.templateId
                it.templateTargetClassId shouldBe expected.templateTargetClassId
                it.label shouldBe expected.label
                it.versions shouldBe expected.versions
                it.observatories shouldBe expected.observatories
                it.organizations shouldBe expected.organizations
                it.extractionMethod shouldBe expected.extractionMethod
                it.visibility shouldBe expected.visibility
                it.unlistedBy shouldBe expected.unlistedBy
                it.modifiable shouldBe expected.modifiable
            }
        }
        it("updates an already existing rosetta stone statement") {
            val original: RosettaStoneStatement = fabricator.random()
            original.requiredEntities().forEach(saveThing)
            repository.save(original)

            val found = repository.findByIdOrVersionId(original.id).get()
            val modified = found.copy(
                label = "some new label, never seen before"
            )
            modified.requiredEntities().forEach(saveThing)
            repository.save(modified)

            repository.findAll(PageRequests.ALL).toSet().size shouldBe 1
            repository.findByIdOrVersionId(original.id).get().asClue {
                it.label shouldBe "some new label, never seen before"
            }
        }
        it("appends new rosetta stone statement versions") {
            val original: RosettaStoneStatement = fabricator.random()
            original.requiredEntities().forEach(saveThing)
            repository.save(original)

            val found = repository.findByIdOrVersionId(original.id).get()
            val newVersion = fabricator.random<RosettaStoneStatementVersion>()
            val modified = found.withVersion(newVersion)
            newVersion.requiredEntities().forEach(saveThing)
            repository.save(modified)

            repository.findAll(PageRequests.ALL).toSet().size shouldBe 1
            repository.findByIdOrVersionId(original.id).get().asClue {
                it.versions shouldBe modified.versions
            }
        }
    }

    describe("finding a rosetta stone statement") {
        it("by id") {
            val expected: RosettaStoneStatement = fabricator.random()
            expected.requiredEntities().forEach(saveThing)
            repository.save(expected)

            val actual = repository.findByIdOrVersionId(expected.id).orElse(null)
            actual shouldBe expected
        }
        it("by version id") {
            val expected: RosettaStoneStatement = fabricator.random()
            expected.requiredEntities().forEach(saveThing)
            repository.save(expected)

            val actual = repository.findByIdOrVersionId(expected.latestVersion.id).orElse(null)
            actual shouldBe expected
        }
    }

    describe("finding several rosetta stone statements") {
        val statements = fabricator.random<List<RosettaStoneStatement>>()
        statements.forEach {
            it.requiredEntities().forEach(saveThing)
            repository.save(it)
        }
        val expected = statements.take(10)

        val result = repository.findAll(PageRequest.of(0, 10))

        it("returns the correct result") {
            result shouldNotBe null
            result.content shouldNotBe null
            result.content.size shouldBe expected.size
            result.content shouldContainAll expected
        }
        it("pages the result correctly") {
            result.size shouldBe 10
            result.number shouldBe 0
            result.totalPages shouldBe 2
            result.totalElements shouldBe statements.size
        }
        xit("sorts the results by creation date by default") {
//            TODO: decide on default sorting key
//            result.content.zipWithNext { a, b ->
//                a.createdAt shouldBeLessThan b.createdAt
//            }
        }
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank") {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is based on uuid v4 format") {
                repository.nextIdentity().value matches Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
            }
        }
        it("returns an id that is not yet in the repository") {
            val statement = fabricator.random<RosettaStoneStatement>().copy(id = repository.nextIdentity())
            statement.requiredEntities().forEach(saveThing)
            repository.save(statement)

            val id = repository.nextIdentity()
            repository.findByIdOrVersionId(id).isPresent shouldBe false
        }
    }

    it("deletes all rosetta stone statements") {
        repeat(3) {
            val statement = fabricator.random<RosettaStoneStatement>().copy(id = ThingId("R$it"))
            statement.requiredEntities().forEach(saveThing)
            repository.save(statement)
        }
        // RosettaStoneStatementRepository has no count method
        repository.findAll(PageRequests.ALL).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequests.ALL).totalElements shouldBe 0
    }
}
