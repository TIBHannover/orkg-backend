package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_PREDICATE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_RESOURCE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_STATEMENT_IDS
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createRdfTriple
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.stream.Stream

internal class RdfTripleProcessorUnitTest : MockkBaseTest {
    private val contributorId = ContributorId("112f3395-4008-4ce4-b6e4-973fbc1b25e2")
    private val extractionMethod = ExtractionMethod.MANUAL
    private val modifiable = true
    private val thingRepository: ThingRepository = mockk()
    private val statementUseCases: StatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val rdfTripleProcessor = RdfTripleProcessor(
        contributorId,
        extractionMethod,
        modifiable,
        thingRepository,
        statementUseCases,
        unsafeLiteralUseCases,
    )

    @Test
    fun `Given an rdf triple, when processing, it creates a new statement and saves the statement id to the job execution context`() {
        val resourceIriToId = mapOf(
            "_:bnode" to ThingId("R123"),
        )
        val predicateIriToId = emptyMap<String, ThingId>()
        val classIriToId = emptyMap<String, ThingId>()
        val statementIds = mutableSetOf<StatementId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
            executionContext.put(RDF_STATEMENT_IDS, statementIds)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val triple = createRdfTriple(
            subject = SimpleValueFactory.getInstance().createBNode("bnode"),
            predicate = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/P123"),
            `object` = SimpleValueFactory.getInstance().createIRI("https://example.org/class"),
        )
        val id = StatementId("S123")

        every {
            thingRepository.findAll(
                pageable = PageRequests.SINGLE,
                uri = IRI.create("https://example.org/class"),
            )
        } returns pageOf(
            createClass(uri = IRI.create("https://example.org/class")),
        )
        every {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectId = any(),
                pageable = PageRequests.SINGLE,
            )
        } returns pageOf()
        every { statementUseCases.create(any()) } returns id

        rdfTripleProcessor.beforeStep(stepExecution)
        rdfTripleProcessor.process(triple) shouldBe null
        rdfTripleProcessor.afterStep(stepExecution)

        statementIds.size shouldBe 1
        statementIds shouldBe setOf(id)

        verify(exactly = 1) { thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create("https://example.org/class")) }
        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectId = any(),
                pageable = PageRequests.SINGLE,
            )
        }
        verify(exactly = 1) {
            statementUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.subjectId shouldBe ThingId("R123")
                    it.predicateId shouldBe ThingId("P123")
                    it.objectId shouldBe ThingId("OK")
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given an rdf triple, when processing and triple contains a literal object, it creates a new literal, a new statement and saves the statement id to the job execution context`() {
        val resourceIriToId = emptyMap<String, ThingId>()
        val predicateIriToId = emptyMap<String, ThingId>()
        val classIriToId = emptyMap<String, ThingId>()
        val statementIds = mutableSetOf<StatementId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
            executionContext.put(RDF_STATEMENT_IDS, statementIds)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val triple = createRdfTriple(
            subject = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/resource/R123"),
            predicate = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/P123"),
            `object` = SimpleValueFactory.getInstance().createLiteral("literal"),
        )
        val id = StatementId("S123")
        val literalId = ThingId("L123")

        every {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectLabel = "literal",
                pageable = PageRequests.SINGLE,
            )
        } returns pageOf()
        every { unsafeLiteralUseCases.create(any()) } returns literalId
        every { statementUseCases.create(any()) } returns id

        rdfTripleProcessor.beforeStep(stepExecution)
        rdfTripleProcessor.process(triple) shouldBe null
        rdfTripleProcessor.afterStep(stepExecution)

        statementIds.size shouldBe 1
        statementIds shouldBe setOf(id)

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectLabel = "literal",
                pageable = PageRequests.SINGLE,
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.label shouldBe "literal"
                    it.datatype shouldBe "xsd:string"
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
        verify(exactly = 1) {
            statementUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.subjectId shouldBe ThingId("R123")
                    it.predicateId shouldBe ThingId("P123")
                    it.objectId shouldBe literalId
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given an rdf triple, when processing and tiple with same entities already exists, it skips the triple`() {
        val resourceIriToId = emptyMap<String, ThingId>()
        val predicateIriToId = emptyMap<String, ThingId>()
        val classIriToId = emptyMap<String, ThingId>()
        val statementIds = mutableSetOf<StatementId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
            executionContext.put(RDF_STATEMENT_IDS, statementIds)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val triple = createRdfTriple(
            subject = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/R123"),
            predicate = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/P123"),
            `object` = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/C123"),
        )

        every {
            statementUseCases.findAll(
                subjectId = ThingId("R123"),
                predicateId = ThingId("P123"),
                objectId = ThingId("C123"),
                pageable = PageRequests.SINGLE,
            )
        } returns pageOf(
            createStatement(
                subject = createResource(ThingId("R123")),
                predicate = createPredicate(ThingId("P123")),
                `object` = createLiteral(ThingId("C123")),
            ),
        )

        rdfTripleProcessor.beforeStep(stepExecution)
        rdfTripleProcessor.process(triple) shouldBe null
        rdfTripleProcessor.afterStep(stepExecution)

        statementIds.size shouldBe 0

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectId = any(),
                pageable = PageRequests.SINGLE,
            )
        }
    }

    @Test
    fun `Given an rdf triple, when processing a tripl with a literal object and statement already exists, then it skips the triple`() {
        val resourceIriToId = emptyMap<String, ThingId>()
        val predicateIriToId = emptyMap<String, ThingId>()
        val classIriToId = emptyMap<String, ThingId>()
        val statementIds = mutableSetOf<StatementId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
            executionContext.put(RDF_STATEMENT_IDS, statementIds)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val triple = createRdfTriple(
            subject = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/resource/R123"),
            predicate = SimpleValueFactory.getInstance().createIRI("$ORKG_VOCAB_PREFIX/predicate/P123"),
            `object` = SimpleValueFactory.getInstance().createLiteral("literal"),
        )

        every {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectLabel = "literal",
                pageable = PageRequests.SINGLE,
            )
        } returns pageOf(
            createStatement(
                subject = createResource(ThingId("R123")),
                predicate = createPredicate(ThingId("P123")),
                `object` = createLiteral(label = "literal"),
            ),
        )

        rdfTripleProcessor.beforeStep(stepExecution)
        rdfTripleProcessor.process(triple) shouldBe null
        rdfTripleProcessor.afterStep(stepExecution)

        statementIds.size shouldBe 0

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = any(),
                predicateId = any(),
                objectLabel = "literal",
                pageable = PageRequests.SINGLE,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("notStatementBasedPredicates")
    fun `Given an rdf triple, when processing and the triple predicate is not statement based in orkg, it skips the triple`(predicate: org.eclipse.rdf4j.model.IRI) {
        val resourceIriToId = emptyMap<String, ThingId>()
        val predicateIriToId = emptyMap<String, ThingId>()
        val classIriToId = emptyMap<String, ThingId>()
        val statementIds = mutableSetOf<StatementId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
            executionContext.put(RDF_STATEMENT_IDS, statementIds)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val triple = createRdfTriple(predicate = predicate)

        rdfTripleProcessor.beforeStep(stepExecution)
        rdfTripleProcessor.process(triple) shouldBe null
        rdfTripleProcessor.afterStep(stepExecution)

        statementIds.size shouldBe 0
    }

    companion object {
        @JvmStatic
        fun notStatementBasedPredicates(): Stream<Arguments> = Stream.of(
            Arguments.of(RDFS.SUBCLASSOF),
            Arguments.of(RDF.TYPE),
            Arguments.of(RDFS.LABEL),
        )
    }
}
