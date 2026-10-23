package org.orkg.dataimport.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.IRI
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping.OrkgEntityType
import org.orkg.dataimport.domain.testing.fixtures.createRdfClass
import org.orkg.dataimport.domain.testing.fixtures.createRdfPredicate
import org.orkg.dataimport.domain.testing.fixtures.createRdfResource
import org.orkg.dataimport.domain.testing.fixtures.createRdfTriple
import org.orkg.dataimport.domain.testing.fixtures.withDataImportMappings
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import java.util.UUID

private val fabricator = Fabrikate(
    FabricatorConfig(
        collectionSizes = 12..12,
        nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull, // FIXME: because "id" is nullable
    )
        .withStandardMappings()
        .withGraphMappings()
        .withDataImportMappings(),
)

interface RdfTripleRepositoryContracts {
    val repository: RdfTripleRepository
    val resourceRepository: RdfResourceRepository
    val classRepository: RdfClassRepository
    val predicateRepository: RdfPredicateRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
        resourceRepository.deleteAll()
        classRepository.deleteAll()
        predicateRepository.deleteAll()
    }

    @Test
    fun `Saving an rdf triple, saves and loads all properties correctly`() {
        val expected: RdfTriple = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual.shouldNotBeNull().asClue {
            it.id shouldBe expected.id
            it.rdfFileId shouldBe expected.rdfFileId
            it.subject shouldBe expected.subject
            it.predicate shouldBe expected.predicate
            it.`object` shouldBe expected.`object`
        }
    }

    @Test
    fun `Finding several rdf triples by rdf file id, returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val expected = fabricator.random<List<RdfTriple>>()
            .map { it.copy(rdfFileId = rdfFileId) }
        expected.forEach(repository::save)

        fabricator.random<List<RdfTriple>>().forEach(repository::save)

        val result = repository.findAllByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = PageRequest.of(0, 15),
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.id.toString() shouldBeLessThan b.id.toString()
        }
    }

    @Test
    fun `Given several rdf triples, when finding all distinct individuals by rdf field id, it returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val triples = listOf(
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                subject = SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/123"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = UUID.randomUUID(),
                subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                predicate = RDF.TYPE,
                `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/class"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                subject = SimpleValueFactory.getInstance().createIRI("https://example.com/subclass"),
                predicate = RDFS.SUBCLASSOF,
                `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/parentclass"),
            ),
        )
        triples.forEach(repository::save)

        val expected = listOf<Resource>(
            SimpleValueFactory.getInstance().createIRI("https://example.com/subject"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/123"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/subject"),
        )

        val result = repository.findAllDistinctIndividualsByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = PageRequest.of(0, 15),
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.toString() shouldBeLessThan b.toString()
        }
    }

    @Test
    fun `Given several rdf triples, when finding all distinct predicates by rdf field id, it returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val triples = listOf(
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = UUID.randomUUID(),
                predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            ),
        )
        triples.forEach(repository::save)

        val expected = listOf<Resource>(
            SimpleValueFactory.getInstance().createIRI("https://example.com/predicate"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
        )

        val result = repository.findAllDistinctPredicatesByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = PageRequest.of(0, 15),
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.toString() shouldBeLessThan b.toString()
        }
    }

    @Test
    fun `Given several rdf triples, when finding all distinct class iris by rdf field id, it returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val triples = listOf(
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                subject = SimpleValueFactory.getInstance().createIRI("https://example.com/class"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/123"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = UUID.randomUUID(),
                subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                predicate = RDF.TYPE,
                `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/class"),
            ),
            createRdfTriple(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
                subject = SimpleValueFactory.getInstance().createIRI("https://example.com/subclass"),
                predicate = RDFS.SUBCLASSOF,
                `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/parentclass"),
            ),
        )
        triples.forEach(repository::save)

        val expected = listOf<Resource>(
            SimpleValueFactory.getInstance().createIRI("https://example.com/class"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/subclass"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/parentclass"),
        )

        val result = repository.findAllDistinctClassIrisByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = PageRequest.of(0, 15),
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.toString() shouldBeLessThan b.toString()
        }
    }

    @Test
    fun `Given several rdf triples, when finding all distinct object iris by rdf field id and subject and predicate, it returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val triples = createTestTriples(rdfFileId)
        triples.forEach(repository::save)

        val expected = setOf<Resource>(
            SimpleValueFactory.getInstance().createIRI("https://example.com/object"),
            SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
        )

        val result = repository.findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/subject"),
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/predicate"),
        )

        result shouldNotBe null
        result.size shouldBe expected.size
        result shouldContainAll expected
    }

    @Test
    fun `Given several rdf triples, when finding all distinct object literals by rdf field id and subject and predicate, it returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val triples = createTestTriples(rdfFileId)
        triples.forEach(repository::save)

        val expected = setOf<Literal>(
            SimpleValueFactory.getInstance().createLiteral("literal"),
        )

        val result = repository.findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/subject"),
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/predicate"),
        )

        result shouldNotBe null
        result.size shouldBe expected.size
        result shouldContainAll expected
    }

    @Test
    fun `Given several rdf things, when finding all distinct resource to type mappings by rdf field id, it returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val resources = listOf(
            createRdfResource(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
            ),
            createRdfResource(
                rdfFileId = UUID.randomUUID(),
                iri = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-resource"),
            ),
        )
        val predicates = listOf(
            createRdfPredicate(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
            ),
            createRdfPredicate(
                rdfFileId = UUID.randomUUID(),
                iri = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-predicate"),
            ),
        )
        val classes = listOf(
            createRdfClass(
                id = UUID.randomUUID(),
                rdfFileId = rdfFileId,
            ),
            createRdfClass(
                rdfFileId = UUID.randomUUID(),
                iri = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-class"),
            ),
        )
        resources.forEach(resourceRepository::save)
        predicates.forEach(predicateRepository::save)
        classes.forEach(classRepository::save)

        val expected = listOf(
            ResourceToTypeMapping(IRI.create("https://example.com/resource"), OrkgEntityType.RESOURCE),
            ResourceToTypeMapping(IRI.create("https://example.com/predicate"), OrkgEntityType.PREDICATE),
            ResourceToTypeMapping(IRI.create("https://example.com/class"), OrkgEntityType.CLASS),
        )

        val result = repository.findAllDistinctResourceToTypeMappingsByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = PageRequest.of(0, 15),
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.iri.toString() shouldBeLessThan b.iri.toString()
        }
    }

    @Test
    fun `Deleting an rdf triple by id, deletes the rdf triple`() {
        val rdfTriple = fabricator.random<RdfTriple>()
        repository.save(rdfTriple)

        repository.count() shouldBe 1

        repository.deleteById(rdfTriple.id)

        repository.findById(rdfTriple.id).isPresent shouldBe false
    }

    @Test
    fun `Deleting all rdf triples by rdf file id, deletes the correct rdf triples`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val triples = fabricator.random<List<RdfTriple>>()
            .map { it.copy(rdfFileId = rdfFileId) }
        triples.forEach(repository::save)

        val expected = fabricator.random<List<RdfTriple>>()
        expected.forEach(repository::save)

        repository.deleteAllByRdfFileId(rdfFileId)

        val actual = repository.findAll()
        actual.count() shouldBe expected.size
        actual shouldContainAll expected
    }

    @Test
    fun `Counting rdf triples, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<RdfTriple>())
        }

        repository.count() shouldBe 3
    }

    private fun createTestTriples(rdfFileId: UUID): List<RdfTriple> = listOf(
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            `object` = SimpleValueFactory.getInstance().createBNode("bnode"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            `object` = SimpleValueFactory.getInstance().createLiteral("literal"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            `object` = SimpleValueFactory.getInstance().createBNode("bnode"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            `object` = SimpleValueFactory.getInstance().createLiteral("literal"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-subject"),
            `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-subject"),
            `object` = SimpleValueFactory.getInstance().createBNode("bnode"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-subject"),
            `object` = SimpleValueFactory.getInstance().createLiteral("literal"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-subject"),
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            `object` = SimpleValueFactory.getInstance().createIRI("https://example.com/other"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-subject"),
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            `object` = SimpleValueFactory.getInstance().createBNode("bnode"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated-subject"),
            predicate = SimpleValueFactory.getInstance().createIRI("https://example.com/unrelated"),
            `object` = SimpleValueFactory.getInstance().createLiteral("literal"),
        ),
        createRdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = UUID.randomUUID(),
        ),
    )
}
