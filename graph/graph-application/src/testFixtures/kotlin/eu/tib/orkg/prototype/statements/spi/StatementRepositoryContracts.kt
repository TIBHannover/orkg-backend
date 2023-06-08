package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.format.DateTimeFormatter.ofPattern
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
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
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
        // Disabled because the expected functionality is not supported by Spring Data Neo4j
        xit("updates an already existing statement") {
            val original = createStatement(
                subject = createResource(ThingId("R1")),
                `object` = createResource(ThingId("R2"))
            )
            saveStatement(original)
            val found = repository.findByStatementId(original.id!!).get()
            found shouldBe original

            val modifiedSubject = createResource(ThingId("R3"))
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
                { createResource(id = ThingId("R$id")) }
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
                    val actual = repository.countByIdRecursive(ThingId("R1"))
                    actual shouldBe 6
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countByIdRecursive(ThingId("missing"))
                    actual shouldBe 0
                }
            }
            context("about a resource") {
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    repository.countStatementsAboutResource(ThingId("R1")) shouldBe 1
                    repository.countStatementsAboutResource(ThingId("R3")) shouldBe 2
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResource(ThingId("missing"))
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
                    ).mapKeys { ThingId("R${it.key}") }
                    val resourceIds = expected.keys + ThingId("R10")
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
                    val actual = repository.countStatementsAboutResources(setOf(ThingId("missing")))
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
        it("does not throw if statement does not exist") {
            val id = StatementId("S123456789")
            repository.findByStatementId(id).isPresent shouldBe false

            repository.deleteByStatementId(id)
        }
    }

    context("deleting several statements") {
        it("by statement id removes them from the repository") {
            val expected: List<GeneralStatement> = fabricator.random()
            expected.forEach(saveStatement)
            repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe expected.size
            repository.deleteByStatementIds(expected.map { it.id!! }.toSet())
            repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe 0
        }
        it("by statement id removes them from the repository (singleton)") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.deleteByStatementIds(setOf(expected.id!!))
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
            val subject = createResource(id = ThingId("R1"))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    subject = subject
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllBySubject(
                subject.id,
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
            val predicate = createPredicate(id = ThingId("P1"))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByPredicateId(
                predicate.id,
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
            val `object` = createResource(id = ThingId("R1"))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    `object` = `object`
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByObject(
                `object`.id,
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
            val `object` = createResource(id = ThingId("R1"))
            val predicate = createPredicate(id = ThingId("P1"))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    `object` = `object`,
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByObjectAndPredicate(
                `object`.id,
                predicate.id,
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
            val subject = createResource(id = ThingId("R1"))
            val predicate = createPredicate(id = ThingId("P1"))
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    subject = subject,
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllBySubjectAndPredicate(
                subject.id,
                predicate.id,
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
            val predicate = createPredicate(id = ThingId("P1"))
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
                predicate.id,
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
            val `class` = ThingId("SomeClass")
            val predicate = createPredicate(id = ThingId("P1"))
            val literal = createLiteral(label = "label to find")
            (0 until expectedCount).forEach {
                statements[it] = statements[it].copy(
                    subject = fabricator.random<Resource>().copy(
                        classes = setOf(`class`)
                    ),
                    `object` = literal,
                    predicate = predicate
                )
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicate.id,
                literal.label,
                `class`,
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
                val subject = createResource(
                    id = ThingId("R$id"),
                    // We need to fix the time here, to make equality work.
                    createdAt = OffsetDateTime.parse("2023-01-24T16:09:18.557233+01:00")
                )
                statements[it] = statements[it].copy(
                    subject = subject
                )
                subject.id
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
            val ids = (0 until 2).map {
                val id = ThingId("R$it")
                val `object` = createResource(id = id)
                statements[it] = statements[it].copy(`object` = `object`)
                if (it == 1)
                    statements[it + 1] = statements[it + 1].copy(`object` = `object`)
                id
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
        context("as a bundle") {
            context("with a minimum level of hops") {
                val statement1 = fabricator.random<GeneralStatement>()
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                saveStatement(statement1)
                saveStatement(statement2)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = 1,
                        maxLevel = null,
                        blacklist = emptyList(),
                        whitelist = emptyList()
                    )
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 1
                    result.first() shouldBe statement2
                }
            }
            context("with a maximum level of hops") {
                val statement1 = fabricator.random<GeneralStatement>()
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                saveStatement(statement1)
                saveStatement(statement2)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = 1,
                        blacklist = emptyList(),
                        whitelist = emptyList()
                    )
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 1
                    result.first() shouldBe statement1
                }
            }
            context("with a blacklist for classes") {
                val statement1 = fabricator.random<GeneralStatement>()
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`,
                    `object` = fabricator.random<Resource>()
                )
                saveStatement(statement1)
                saveStatement(statement2)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = null,
                        blacklist = (statement2.`object` as Resource).classes.take(2),
                        whitelist = emptyList()
                    )
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 1
                    result.first() shouldBe statement1
                }
            }
            context("with a whitelist for classes") {
                val statement1 = fabricator.random<GeneralStatement>().copy(
                    `object` = fabricator.random<Resource>()
                )
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                saveStatement(statement1)
                saveStatement(statement2)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = null,
                        blacklist = emptyList(),
                        whitelist = (statement1.`object` as Resource).classes.take(2)
                    )
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 1
                    result.first() shouldBe statement1
                }
            }
        }
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank") {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is prefixed with 'S'") {
                repository.nextIdentity().value[0] shouldBe 'S'
            }
        }
        it("returns an id that is not yet in the repository") {
            val statement = createStatement(id = repository.nextIdentity())
            saveStatement(statement)
            val id = repository.nextIdentity()
            repository.findByStatementId(id).isPresent shouldBe false
        }
    }

    describe("finding a doi") {
        context("by contribution id") {
            val statements = mutableListOf<GeneralStatement>()
            val hasContribution = createPredicate(
                id = ThingId("P31")
            )
            val hasDOI = createPredicate(
                id = ThingId("P26")
            )
            repeat(2) {
                val paper = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Paper"))
                )
                val contribution = createResource(
                    id = fabricator.random(),
                )
                val doi = createLiteral(
                    id = fabricator.random(),
                    label = fabricator.random()
                )
                val paperHasContribution = createStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = hasContribution,
                    `object` = contribution
                )
                val paperHasDoi = createStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = hasDOI,
                    `object` = doi
                )
                statements.add(paperHasContribution)
                statements.add(paperHasDoi)
            }

            it("returns the correct result") {
                val contribution = statements[0].`object` as Resource
                statements.forEach(saveStatement)

                val expected = statements[1].`object`
                val actual = repository.findDOIByContributionId(contribution.id)

                actual.isPresent shouldBe true
                actual.get() shouldBe expected
            }
        }
    }

    describe("counting predicate usage") {
        context("for a single predicate") {
            context("when no statements exist") {
                it("returns the correct result") {
                    val actual = repository.countPredicateUsage(ThingId("Missing"))
                    actual shouldBe 0
                }
            }
            context("when used in a statement") {
                context("as a predicate") {
                    it("returns the correct result") {
                        val statement = fabricator.random<GeneralStatement>()
                        saveStatement(statement)

                        val actual = repository.countPredicateUsage(statement.predicate.id)
                        actual shouldBe 1
                    }
                }
                context("as a subject") {
                    it("returns the correct result") {
                        val subject = fabricator.random<Predicate>()
                        val statement = fabricator.random<GeneralStatement>().copy(
                            subject = subject
                        )
                        saveStatement(statement)

                        val actual = repository.countPredicateUsage(subject.id)
                        actual shouldBe 1
                    }
                }
                context("as a subject") {
                    it("returns the correct result") {
                        val `object` = fabricator.random<Predicate>()
                        val statement = fabricator.random<GeneralStatement>().copy(
                            `object` = `object`
                        )
                        saveStatement(statement)

                        val actual = repository.countPredicateUsage(`object`.id)
                        actual shouldBe 1
                    }
                }
            }
        }
        context("for all predicates") {
            context("when no statements exist") {
                val result = repository.countPredicateUsage(PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 0
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 0
                    result.totalElements shouldBe 0
                }
            }
            context("when several statements exist") {
                val statements = fabricator.random<MutableList<GeneralStatement>>()
                statements[1] = statements[1].copy(
                    predicate = statements[0].predicate
                )
                statements[2] = statements[2].copy(
                    predicate = statements[0].predicate
                )
                statements.forEach(saveStatement)

                val expected = statements.drop(3)
                    .map { PredicateUsageCount(it.predicate.id, 1) }
                    .plus(PredicateUsageCount(statements[0].predicate.id, 3))
                    .sortedWith(compareByDescending<PredicateUsageCount> { it.count }.thenBy { it.id })

                val result = repository.countPredicateUsage(PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 5
                    result.content shouldContainAll expected.take(5)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 2
                    result.totalElements shouldBe 10
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.count shouldBeGreaterThanOrEqual b.count
                    }
                }
            }
        }
    }

    describe("finding a paper") {
        val doi = fabricator.random<String>()
        val hasDoi = createPredicate(id = ThingId("P26"))
        context("by doi") {
            it("returns the correct result") {
                val paper = createResource(classes = setOf(ThingId("Paper")))
                val paperHasDoi = createStatement(
                    subject = paper,
                    predicate = hasDoi,
                    `object` = createLiteral(label = doi)
                )
                saveStatement(paperHasDoi)

                val actual = repository.findByDOI(doi)
                actual.isPresent shouldBe true
                actual.get() shouldBe paper

                val upper = repository.findByDOI(doi.uppercase())
                upper.isPresent shouldBe true
                upper.get() shouldBe paper

                val lower = repository.findByDOI(doi.lowercase())
                lower.isPresent shouldBe true
                lower.get() shouldBe paper
            }
            it("does not return deleted papers") {
                val paper = createResource(
                    classes = setOf(ThingId("Paper"), ThingId("PaperDeleted"))
                )
                val paperHasDoi = createStatement(
                    subject = paper,
                    predicate = hasDoi,
                    `object` = createLiteral(label = doi)
                )
                saveStatement(paperHasDoi)

                val actual = repository.findByDOI(doi)
                actual.isPresent shouldBe false
            }
        }
    }

    describe("finding several research problems") {
        context("by observatory id") {
            val observatoryId = fabricator.random<ObservatoryId>()
            val expected = (0 until 2).map {
                val paper = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Paper")),
                    observatoryId = observatoryId
                )
                val contribution = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Contribution"))
                )
                val researchProblem = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Problem")),
                    observatoryId = observatoryId
                )
                val paperHasContribution = createStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = createPredicate(ThingId("P31")), // hasContribution
                    `object` = contribution
                )
                val contributionHasResearchProblem = createStatement(
                    id = fabricator.random(),
                    subject = contribution,
                    predicate = createPredicate(ThingId("P32")), // hasProblem
                    `object` = researchProblem
                )
                saveStatement(paperHasContribution)
                saveStatement(contributionHasResearchProblem)
                researchProblem
            }

            val result = repository.findProblemsByObservatoryId(observatoryId, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by organization id") {
            val organizationId = fabricator.random<OrganizationId>()
            val compareContribution = fabricator.random<Predicate>().copy(
                id = ThingId("compareContribution")
            )
            val hasResearchProblem = fabricator.random<Predicate>().copy(
                id = ThingId("P32")
            )
            val expected = (0 until 2).map {
                val contribution = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Contribution"))
                )
                val researchProblem = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Problem"))
                )
                val comparison = createResource(
                    id = fabricator.random(),
                    classes = setOf(ThingId("Comparison")),
                    organizationId = organizationId
                )
                val comparisonHasContribution = createStatement(
                    id = fabricator.random(),
                    subject = comparison,
                    predicate = compareContribution,
                    `object` = contribution
                )
                val contributionHasResearchProblem = createStatement(
                    id = fabricator.random(),
                    subject = contribution,
                    predicate = hasResearchProblem,
                    `object` = researchProblem
                )
                saveStatement(comparisonHasContribution)
                saveStatement(contributionHasResearchProblem)
                researchProblem
            }

            val result = repository.findAllProblemsByOrganizationId(organizationId, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
    }

    describe("finding a timeline") {
        context("by resource id") {
            val resource = fabricator.random<Resource>().copy(
                createdAt = OffsetDateTime.now()
            )

            setOf("ResearchField", "ResearchProblem", "Paper").forEach {
                val resourceForIt = fabricator.random<Resource>().copy(
                    classes = setOf(ThingId(it)),
                    createdAt = resource.createdAt.plusSeconds(145864)
                )
                val resourceRelatesToIt = fabricator.random<GeneralStatement>().copy(
                    subject = resource,
                    `object` = resourceForIt,
                    createdAt = resource.createdAt.plusSeconds(7897)
                )
                saveStatement(resourceRelatesToIt)
            }

            // Relate to some other Resource
            val otherResource = fabricator.random<Resource>().copy(
                createdAt = resource.createdAt.plusSeconds(5478)
            )
            val resourceRelatesToOtherResource = fabricator.random<GeneralStatement>().copy(
                subject = resource,
                `object` = otherResource,
                createdAt = resource.createdAt.plusSeconds(26158)
            )
            saveStatement(resourceRelatesToOtherResource)

            // Relate otherResource to another Resource
            val anotherResource = fabricator.random<Resource>().copy(
                createdAt = resource.createdAt.plusSeconds(9871)
            )
            val otherResourceRelatesToAnotherResource = fabricator.random<GeneralStatement>().copy(
                subject = otherResource,
                `object` = anotherResource,
                createdAt = resource.createdAt.plusSeconds(14659)
            )
            saveStatement(otherResourceRelatesToAnotherResource)

            // Relate to an old Resource
            val oldResource = fabricator.random<Resource>().copy(
                createdAt = resource.createdAt.minusSeconds(651456)
            )
            val resourceRelatesToOldResource = fabricator.random<GeneralStatement>().copy(
                subject = resource,
                `object` = oldResource,
                createdAt = resource.createdAt.minusSeconds(156168)
            )
            saveStatement(resourceRelatesToOldResource)

            val formatter = ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            val expected = setOf(
                resource.createdBy to resource.createdAt,
                otherResource.createdBy to otherResource.createdAt,
                resourceRelatesToOtherResource.createdBy to resourceRelatesToOtherResource.createdAt,
                anotherResource.createdBy to anotherResource.createdAt,
                otherResourceRelatesToAnotherResource.createdBy to otherResourceRelatesToAnotherResource.createdAt
            ).map {
                ResourceContributor(
                    it.first.toString(),
                    it.second!!
                        .withSecond(0)
                        .withNano(0)
                        .atZoneSameInstant(ZoneOffset.UTC)
                        .format(formatter)
                )
            }

            val result = repository.findTimelineByResourceId(resource.id, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 5
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 5
            }
            it("sorts the results by creation date by default") {
                result.content.map {
                    OffsetDateTime.parse(it.createdAt, ISO_OFFSET_DATE_TIME)
                }.zipWithNext { a, b ->
                    a shouldBeGreaterThan b
                }
            }
        }
    }

    describe("finding several contributors") {
        context("by resource id") {
            val resource = fabricator.random<Resource>()

            setOf("ResearchField", "ResearchProblem", "Paper").forEach {
                val resourceForIt = fabricator.random<Resource>().copy(
                    classes = setOf(ThingId(it))
                )
                val resourceRelatesToIt = fabricator.random<GeneralStatement>().copy(
                    subject = resource,
                    `object` = resourceForIt
                )
                saveStatement(resourceRelatesToIt)
            }

            // Relate to some other Resource
            val otherResource = fabricator.random<Resource>()
            val resourceRelatesToOtherResource = fabricator.random<GeneralStatement>().copy(
                subject = resource,
                `object` = otherResource
            )
            saveStatement(resourceRelatesToOtherResource)

            // Relate otherResource to another Resource
            val anotherResource = fabricator.random<Resource>()
            val otherResourceRelatesToAnotherResource = fabricator.random<GeneralStatement>().copy(
                subject = otherResource,
                `object` = anotherResource
            )
            saveStatement(otherResourceRelatesToAnotherResource)

            val expected = setOf(
                resource.createdBy,
                otherResource.createdBy,
                resourceRelatesToOtherResource.createdBy,
                anotherResource.createdBy,
                otherResourceRelatesToAnotherResource.createdBy
            )

            val result = repository.findAllContributorsByResourceId(resource.id, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 5
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 5
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.value.toString() shouldBeLessThan b.value.toString()
                }
            }
        }
    }

    describe("checking if a resource is used in a statement") {
        context("when no statements exist") {
            it("returns the correct result") {
                val resource = fabricator.random<Resource>()
                // Resource has to exist for neo4j repos
                resourceRepository.save(resource)
                repository.checkIfResourceHasStatements(resource.id) shouldBe false
            }
        }
        context("when a statement exists") {
            it("returns the correct result") {
                val statement = fabricator.random<GeneralStatement>().copy(
                    subject = fabricator.random<Resource>(),
                    `object` = fabricator.random<Resource>()
                )
                saveStatement(statement)
                repository.checkIfResourceHasStatements(statement.subject.id) shouldBe true
                repository.checkIfResourceHasStatements(statement.`object`.id) shouldBe true
            }
        }
    }

    describe("finding a statement") {
        context("by subject id and predicate id and object id") {
            it("returns the correct result") {
                val statements = fabricator.random<List<GeneralStatement>>()
                statements.forEach(saveStatement)

                val expected = statements[0]

                val actual = repository.findBySubjectIdAndPredicateIdAndObjectId(
                    subjectId = expected.subject.id,
                    predicateId = expected.predicate.id,
                    objectId = expected.`object`.id
                )

                actual.isPresent shouldBe true
                actual.get() shouldBe expected
            }
        }
    }
    describe("determining ownership") {
        context("when multiple ids are given") {
            it("returns the correct result") {
                val statements = fabricator.random<List<GeneralStatement>>()
                statements.forEach(saveStatement)
                val allStatementIds = statements.map { it.id!! }.toSet()
                val expected = statements.map { OwnershipInfo(it.id!!, it.createdBy) }.toSet()

                val actual = repository.determineOwnership(allStatementIds)

                actual shouldBe expected
            }
        }
        context("when one id is given") {
            it("returns the correct result") {
                val statement = fabricator.random<GeneralStatement>()
                saveStatement(statement)
                val expected = setOf(statement).map { OwnershipInfo(it.id!!, it.createdBy) }.toSet()

                val actual = repository.determineOwnership(setOf(statement.id!!))

                actual shouldBe expected
            }
        }
        context("when no id is given") {
            it("returns the correct result") {
                val expected = emptySet<OwnershipInfo>()

                val actual = repository.determineOwnership(emptySet())

                actual shouldBe expected
            }
        }
    }
}
