package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
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
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.orkg.testing.MockUserId
import org.orkg.testing.fixedClock
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

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
            collectionSizes = 5..5,
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
            is Predicate -> predicateRepository.save(it)
        }
    }

    val saveStatement: (GeneralStatement) -> Unit = {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        statementRepository.save(it)
    }

    fun RosettaStoneStatementVersion.requiredEntities(): Set<Thing> =
        subjects.toSet() + objects.flatten()

    fun RosettaStoneStatement.requiredEntities(): Set<Thing> =
        setOfNotNull(
            fabricator.random<Resource>().copy(id = templateId, classes = setOf(Classes.rosettaNodeShape)),
            contextId?.let { fabricator.random<Resource>().copy(id = it) },
            fabricator.random<Class>().copy(id = templateTargetClassId),
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
        context("without filters") {
            val statements = fabricator.random<List<RosettaStoneStatement>>()
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.sortedBy { it.versions.first().createdAt }.take(10)
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
                result.totalPages shouldBe 1
                result.totalElements shouldBe statements.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by context") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            val contextId = fabricator.random<ThingId>()
            statements.take(3).forEachIndexed { index, statement ->
                statements[index] = statement.copy(contextId = contextId)
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3).sortedBy { it.versions.first().createdAt }
            val result = repository.findAll(PageRequest.of(0, 10), context = contextId)

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by template id") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            val templateId = fabricator.random<ThingId>()
            statements.take(3).forEachIndexed { index, statement ->
                statements[index] = statement.copy(templateId = templateId)
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3).sortedBy { it.versions.first().createdAt }
            val result = repository.findAll(PageRequest.of(0, 10), templateId = templateId)

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by class id") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            val classId = fabricator.random<ThingId>()
            statements.take(3).forEachIndexed { index, statement ->
                statements[index] = statement.copy(templateTargetClassId = classId)
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3).sortedBy { it.versions.first().createdAt }
            val result = repository.findAll(PageRequest.of(0, 10), templateTargetClassId = classId)

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by visibility") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            statements.forEachIndexed { index, statement ->
                statements[index] = statement.copy(visibility = Visibility.entries[index % Visibility.entries.size])
            }
            VisibilityFilter.entries.forEach { visibilityFilter ->
                context("when visibility is $visibilityFilter") {
                    statements.forEach {
                        it.requiredEntities().forEach(saveThing)
                        repository.save(it)
                    }
                    val expected = statements.filter { it.visibility in visibilityFilter.targets }.sortedBy { it.versions.first().createdAt }
                    val result = repository.findAll(PageRequest.of(0, 10), visibility = visibilityFilter)

                    it("returns the correct result") {
                        result shouldNotBe null
                        result.content shouldNotBe null
                        result.content.size shouldBe expected.size
                        result.content shouldContainAll expected
                    }
                    it("pages the result correctly") {
                        result.size shouldBe 10
                        result.number shouldBe 0
                        result.totalPages shouldBe 1
                        result.totalElements shouldBe expected.size
                    }
                    it("sorts the results by creation date by default") {
                        result.content.zipWithNext { a, b ->
                            a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                        }
                    }
                }
            }
        }
        context("by created by") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            val contributorId = fabricator.random<ContributorId>()
            statements.take(3).forEachIndexed { index, statement ->
                statements[index] = statement.copy(
                    versions = statement.versions.toMutableList().also { versions ->
                        versions[0] = versions[0].copy(createdBy = contributorId)
                    }
                )
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3).sortedBy { it.versions.first().createdAt }
            val result = repository.findAll(PageRequest.of(0, 10), createdBy = contributorId)

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by created at start") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            statements.forEachIndexed { index, statement ->
                statements[index] = statement.copy(
                    versions = statement.versions.toMutableList().also { versions ->
                        versions[0] = versions[0].copy(createdAt = OffsetDateTime.now().minusHours(index.toLong()))
                    }
                )
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 10),
                createdAtStart = expected.last().versions.first().createdAt
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by created at end") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            statements.forEachIndexed { index, statement ->
                statements[index] = statement.copy(
                    versions = statement.versions.toMutableList().also { versions ->
                        versions[0] = versions[0].copy(createdAt = OffsetDateTime.now().plusHours(index.toLong()))
                    }
                )
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 10),
                createdAtEnd = expected.last().versions.first().createdAt
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by organization id") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            val organizationId = fabricator.random<OrganizationId>()
            statements.take(3).forEachIndexed { index, statement ->
                statements[index] = statement.copy(organizations = listOf(organizationId))
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3).sortedBy { it.versions.first().createdAt }

            val result = repository.findAll(PageRequest.of(0, 10), organizationId = organizationId)

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("by observatory id") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            val observatoryId = fabricator.random<ObservatoryId>()
            statements.take(3).forEachIndexed { index, statement ->
                statements[index] = statement.copy(observatories = listOf(observatoryId))
            }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.take(3).sortedBy { it.versions.first().createdAt }
            val result = repository.findAll(PageRequest.of(0, 10), observatoryId = observatoryId)

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expected.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        context("with all filters") {
            val statements = fabricator.random<List<RosettaStoneStatement>>()
                .map { it.copy(visibility = Visibility.FEATURED) }
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val expected = statements.first()
            val firstVersion = expected.versions.first()
            val result = repository.findAll(
                pageable = PageRequest.of(0, 10),
                context = expected.contextId,
                templateId = expected.templateId,
                templateTargetClassId = expected.templateTargetClassId,
                visibility = VisibilityFilter.ALL_LISTED,
                createdBy = firstVersion.createdBy,
                createdAtStart = firstVersion.createdAt,
                createdAtEnd = firstVersion.createdAt,
                observatoryId = expected.observatories.single(),
                organizationId = expected.organizations.single()
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(expected)
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.versions.first().createdAt shouldBeLessThan b.versions.first().createdAt
                }
            }
        }
        it("sorts the results by multiple properties") {
            val statements = fabricator.random<MutableList<RosettaStoneStatement>>()
            statements[1] = statements[1].copy(
                versions = statements[1].versions.toMutableList().also { versions ->
                    versions[0] = versions[0].copy(createdBy = statements[0].versions.first().createdBy)
                }
            )
            statements.forEach {
                it.requiredEntities().forEach(saveThing)
                repository.save(it)
            }
            val sort = Sort.by("created_by").ascending().and(Sort.by("created_at").descending())
            val result = repository.findAll(PageRequest.of(0, 12, sort))

            result.content.zipWithNext { a, b ->
                if (a.versions.first().createdBy == b.versions.first().createdBy) {
                    a.versions.first().createdAt shouldBeGreaterThan b.versions.first().createdAt
                } else {
                    a.versions.first().createdBy.value.toString() shouldBeLessThan b.versions.first().createdBy.value.toString()
                }
            }
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

    it("soft deletes a rosetta stone statement") {
        val statement = fabricator.random<RosettaStoneStatement>()
        statement.requiredEntities().forEach(saveThing)
        repository.save(statement)
        val contributorId = ContributorId(MockUserId.USER)

        repository.softDelete(statement.id, contributorId)

        repository.findByIdOrVersionId(statement.id).asClue { optional ->
            optional.isPresent shouldBe true
            optional.get().asClue {
                it.visibility shouldBe Visibility.DELETED
                it.versions.forEach { version ->
                    version.visibility shouldBe Visibility.DELETED
                    version.deletedBy shouldBe contributorId
                    version.deletedAt shouldBe OffsetDateTime.now(fixedClock)
                }
            }
        }
    }

    it("deletes a rosetta stone statement") {
        val statement = fabricator.random<RosettaStoneStatement>()
        statement.requiredEntities().forEach(saveThing)
        repository.save(statement)

        repository.delete(statement.id)

        repository.findByIdOrVersionId(statement.id).isPresent shouldBe false
    }

    describe("checking whether a rosetta stone statement is used as an object") {
        context("when it is not used as an object") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                repository.isUsedAsObject(statement.id) shouldBe false
                repository.isUsedAsObject(statement.versions.first().id) shouldBe false
            }
        }
        context("when the latest version is used as a subject in another rosetta stone statement") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                val statementResource = resourceRepository.findById(statement.id).get()
                val otherStatement = fabricator.random<RosettaStoneStatement>().let {
                    it.copy(versions = it.versions + it.versions.first().copy(
                        id = fabricator.random(),
                        subjects = listOf(statementResource)
                    ))
                }
                otherStatement.requiredEntities().forEach(saveThing)
                repository.save(otherStatement)

                repository.isUsedAsObject(statement.id) shouldBe true
                repository.isUsedAsObject(statement.versions.first().id) shouldBe true
            }
        }
        context("when an older version is used as a subject in another rosetta stone statement") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                val statementResource = resourceRepository.findById(statement.versions.last().id).get()
                val otherStatement = fabricator.random<RosettaStoneStatement>().let {
                    it.copy(versions = it.versions + it.versions.first().copy(
                        id = fabricator.random(),
                        subjects = listOf(statementResource)
                    ))
                }
                otherStatement.requiredEntities().forEach(saveThing)
                repository.save(otherStatement)

                repository.isUsedAsObject(statement.id) shouldBe true
                repository.isUsedAsObject(statement.versions.first().id) shouldBe true
            }
        }
        context("when the latest version is used as an object in another rosetta stone statement") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                val statementResource = resourceRepository.findById(statement.id).get()
                val otherStatement = fabricator.random<RosettaStoneStatement>().let {
                    it.copy(versions = it.versions + it.versions.first().copy(
                        id = fabricator.random(),
                        objects = listOf(listOf(statementResource))
                    ))
                }
                otherStatement.requiredEntities().forEach(saveThing)
                repository.save(otherStatement)

                repository.isUsedAsObject(statement.id) shouldBe true
                repository.isUsedAsObject(statement.versions.first().id) shouldBe true
            }
        }
        context("when an older version is used as an object in another rosetta stone statement") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                val statementResource = resourceRepository.findById(statement.versions.last().id).get()
                val otherStatement = fabricator.random<RosettaStoneStatement>().let {
                    it.copy(versions = it.versions + it.versions.first().copy(
                        id = fabricator.random(),
                        objects = listOf(listOf(statementResource))
                    ))
                }
                otherStatement.requiredEntities().forEach(saveThing)
                repository.save(otherStatement)

                repository.isUsedAsObject(statement.id) shouldBe true
                repository.isUsedAsObject(statement.versions.first().id) shouldBe true
            }
        }
        context("when the latest version is used in a triple") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        `object` = resourceRepository.findById(statement.id).get()
                    )
                )

                repository.isUsedAsObject(statement.id) shouldBe true
                repository.isUsedAsObject(statement.versions.first().id) shouldBe true
            }
        }
        context("when an older version is used in a triple") {
            it("returns the correct result") {
                val statement = fabricator.random<RosettaStoneStatement>()
                statement.requiredEntities().forEach(saveThing)
                repository.save(statement)

                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        `object` = resourceRepository.findById(statement.versions.last().id).get()
                    )
                )

                repository.isUsedAsObject(statement.id) shouldBe true
                repository.isUsedAsObject(statement.versions.first().id) shouldBe true
            }
        }
    }
}
