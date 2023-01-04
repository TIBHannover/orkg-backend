package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.createLiteral
import org.orkg.statements.testing.createPredicate
import org.orkg.statements.testing.createResource
import org.orkg.statements.testing.createStatement
import org.orkg.statements.testing.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> statementRepositoryContract(
    repository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P
) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()
    
    val saveThing: (Thing) -> Unit = {
        when (it) {
            is Class -> classRepository.save(it)
            is Literal -> literalRepository.save(it)
            is Resource -> resourceRepository.save(it)
            is Predicate -> predicateRepository.save(it)
        }
    }

    val saveStatement: (GeneralStatement) -> Unit = {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        repository.save(it)
    }

    describe("saving a statement") {
        it("saves and loads all properties correctly") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)

            val actual = repository.findByStatementId(expected.id!!).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.subject shouldBe expected.subject // FIXME: deep check
                it.predicate shouldBe expected.predicate // FIXME: deep check
                it.`object` shouldBe expected.`object` // FIXME: deep check
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
            }
        }
        it("updates an already existing statement") {
            val original = createStatement(
                subject = createResource(ResourceId(1)),
                `object` = createResource(ResourceId(2))
            )
            saveStatement(original)
            val found = repository.findByStatementId(original.id!!).get()
            val modifiedSubject = createResource(ResourceId(3))
            val modified = found.copy(subject = modifiedSubject)
            saveStatement(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findByStatementId(original.id!!).get().subject shouldBe modifiedSubject
        }
    }

    describe("counting statements") {
        context("returns the correct result when") {
            it("some statements exist") {
                (0L until 3L).forEach {
                    saveStatement(createStatement(id = StatementId(it)))
                }
                repository.count() shouldBe 3
            }
            it("no statements exist") {
                repository.count() shouldBe 0
            }
        }
        context("in a graph") {
            //     6   4 → 7  8 → 9
            //     ↓   ↑   ↓
            // 5 ← 1 → 2 → 3
            val resources = mutableMapOf<Int, Resource>()
            val resourceFactory = { id: Int ->
                { createResource(id = ResourceId(id.toLong())) }
            }
            val graph = listOf(
                1 to 2, 2 to 3, 2 to 4, 4 to 7,
                7 to 3, 1 to 5, 6 to 1, 8 to 9
            ).map {
                val subject = resources.getOrPut(it.first, resourceFactory(it.first))
                val `object` = resources.getOrPut(it.second, resourceFactory(it.second))
                val statement = createStatement(
                    id = fabricator.random(),
                    subject = subject,
                    `object` = `object`
                )
                statement
            }
            context("recursively by resource id") {
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    val actual = repository.countByIdRecursive(ResourceId(1).value)
                    actual shouldBe 6
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countByIdRecursive("missing")
                    actual shouldBe 0
                }
            }
            context("about a resource") {
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    repository.countStatementsAboutResource(ResourceId(1)) shouldBe 1
                    repository.countStatementsAboutResource(ResourceId(3)) shouldBe 2
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResource(ResourceId("missing"))
                    actual shouldBe 0
                }
            }
            context("about several resources") {
                // TODO: do we expect results for missing resource ids to be zero or missing?
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    val expected = mapOf(
                        1L to 1L,
                        3L to 2L,
                        //10L to 0L
                    ).mapKeys { ResourceId(it.key) }
                    val resourceIds = expected.keys + ResourceId(10)
                    val actual = repository.countStatementsAboutResources(resourceIds)
                    actual shouldContainExactly expected
                }
                it("returns empty result when no ids are given") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResources(setOf())
                    actual.size shouldBe 0
                }
                // TODO: do we expect results for missing resource ids to be zero or missing?
                it("returns nothing when the given resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResources(setOf(ResourceId("missing")))
                    actual.size shouldBe 0
                }
            }
        }
    }

    context("deleting a statement") {
        it("by statement instance removes it from the repository") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.delete(expected)
            repository.findByStatementId(expected.id!!).isPresent shouldBe false
        }
        it("by statement id removes it from the repository") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.deleteByStatementId(expected.id!!)
            repository.findByStatementId(expected.id!!).isPresent shouldBe false
        }
    }

    it("delete all statements") {
        (0L until 3L).forEach {
            saveStatement(createStatement(id = StatementId(it)))
        }
        repository.count() shouldBe 3
        repository.deleteAll()
        repository.count() shouldBe 0
    }

    describe("finding several statements") {
        context("by subject id") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val subject = createResource(id = ResourceId(1))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    subject = subject
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllBySubject(
                subject.id.toString(),
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by predicate id") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val predicate = createPredicate(id = PredicateId(1))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByPredicateId(
                predicate.id!!,
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by object id") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val `object` = createResource(id = ResourceId(1))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    `object` = `object`
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByObject(
                `object`.id.toString(),
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by object id and predicate id") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val `object` = createResource(id = ResourceId(1))
            val predicate = createPredicate(id = PredicateId(1))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    `object` = `object`,
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByObjectAndPredicate(
                `object`.id.toString(),
                predicate.id!!,
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by subject id and predicate id") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val subject = createResource(id = ResourceId(1))
            val predicate = createPredicate(id = PredicateId(1))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    subject = subject,
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllBySubjectAndPredicate(
                subject.id.toString(),
                predicate.id!!,
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by predicate id and object label") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val literal = createLiteral(label = "label to find")
            val predicate = createPredicate(id = PredicateId(1))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    `object` = literal,
                    predicate = predicate
                )
            }
            // Create a statement with an object that is not a literal but has the same label
            val `object` = createResource(label = "label to find")
            statements[expectedCount] = statements[expectedCount].copy(
                `object` = `object`,
                predicate = predicate
            )
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByPredicateIdAndLabel(
                predicate.id!!,
                literal.label,
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by predicate id and object label and subject class id") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val subject = createClass(id = ClassId(1))
            val predicate = createPredicate(id = PredicateId(1))
            val literal = createLiteral(label = "label to find")
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    subject = subject,
                    `object` = literal,
                    predicate = predicate
                )
            }
            // Create a statement with an object that is not a literal but has the same label
            val `object` = createResource(label = "label to find")
            statements[expectedCount] = statements[expectedCount].copy(
                subject = subject,
                `object` = `object`,
                predicate = predicate
            )
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicate.id!!,
                literal.label,
                subject.id!!,
                PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by subject ids") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val ids = (0 until expectedCount).map {
                // we generate the following mapping:
                // index(0) -> id(0)
                // index(1) -> id(1)
                // index(2) -> id(1)
                val id = it.coerceAtMost(1).toLong()
                val subject = createResource(id = ResourceId(id))
                statements[it] = statements[it].copy(
                    subject = subject
                )
                subject.id!!.value
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllBySubjects(ids, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by object ids") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val ids = (0 until expectedCount).map {
                // we generate the following mapping:
                // index(0) -> id(0)
                // index(1) -> id(1)
                // index(2) -> id(1)
                val id = it.coerceAtMost(1).toLong()
                val `object` = createResource(id = ResourceId(id))
                statements[it] = statements[it].copy(
                    `object` = `object`
                )
                `object`.id!!.value
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByObjects(ids, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
    }

    context("requesting a new identity") {
        it("returns a valid id") {
            repository.nextIdentity() shouldNotBe null
        }
        it("returns an id that is not yet in the repository") {
            val statement = createStatement(id = repository.nextIdentity())
            saveStatement(statement)
            val id = repository.nextIdentity()
            repository.findByStatementId(id).isPresent shouldBe false
        }
    }
}
