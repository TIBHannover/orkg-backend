package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_RESOURCE_IRI_TO_ID
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createRdfResource
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf

internal class RdfResourceProcessorUnitTest : MockkBaseTest {
    private val contributorId = ContributorId("112f3395-4008-4ce4-b6e4-973fbc1b25e2")
    private val extractionMethod = ExtractionMethod.MANUAL
    private val modifiable = true
    private val thingRepository: ThingRepository = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val rdfResourceProcessor = RdfResourceProcessor(
        contributorId,
        extractionMethod,
        modifiable,
        thingRepository,
        unsafeResourceUseCases,
    )

    @Test
    fun `Given an rdf resource, when processing and resource does not already exist, it creates a new resource and saves the resource iri to id mapping to the job execution context`() {
        val resourceIriToId = mutableMapOf<String, ThingId>()
        val classIriToId = mapOf(
            "https://example.org/class" to ThingId("C123"),
        )
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val resource = createRdfResource(
            classes = setOf(
                SimpleValueFactory.getInstance().createIRI("https://example.org/OK"),
                SimpleValueFactory.getInstance().createIRI("https://example.org/class"),
            ),
        )
        val id = ThingId("R123")

        every { thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create(resource.iri.toString())) } returns pageOf()
        every {
            thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create("https://example.org/OK"))
        } returns pageOf(createClass())
        every { unsafeResourceUseCases.create(any()) } returns id

        rdfResourceProcessor.beforeStep(stepExecution)
        rdfResourceProcessor.process(resource) shouldBe null
        rdfResourceProcessor.afterStep(stepExecution)

        resourceIriToId.size shouldBe 1
        resourceIriToId shouldContainExactly mapOf(
            resource.iri.toString() to id,
        )

        verify(exactly = 1) { thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create(resource.iri.toString())) }
        verify(exactly = 1) { thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create("https://example.org/OK")) }
        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.label shouldBe resource.label
                    it.uri shouldBe IRI.create(resource.iri.toString())
                    it.classes shouldBe setOf(ThingId("OK"), ThingId("C123"))
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given an rdf resource, when processing and resource is a blank node, it creates a new resource and saves the resource iri to id mapping to the job execution context`() {
        val resourceIriToId = mutableMapOf<String, ThingId>()
        val classIriToId = mapOf(
            "https://example.org/class" to ThingId("C123"),
        )
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val resource = createRdfResource(
            iri = SimpleValueFactory.getInstance().createBNode("bnode"),
            classes = setOf(
                SimpleValueFactory.getInstance().createIRI("https://example.org/OK"),
                SimpleValueFactory.getInstance().createIRI("https://example.org/class"),
            ),
        )
        val id = ThingId("R123")

        every {
            thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create("https://example.org/OK"))
        } returns pageOf(createClass())
        every { unsafeResourceUseCases.create(any()) } returns id

        rdfResourceProcessor.beforeStep(stepExecution)
        rdfResourceProcessor.process(resource) shouldBe null
        rdfResourceProcessor.afterStep(stepExecution)

        resourceIriToId.size shouldBe 1
        resourceIriToId shouldContainExactly mapOf(
            resource.iri.toString() to id,
        )

        verify(exactly = 1) { thingRepository.findAll(pageable = PageRequests.SINGLE, uri = IRI.create("https://example.org/OK")) }
        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.label shouldBe resource.label
                    it.uri shouldBe null
                    it.classes shouldBe setOf(ThingId("OK"), ThingId("C123"))
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given an rdf resource, when processing and resource already exists, it does not create a new resource`() {
        val resourceIriToId = mutableMapOf<String, ThingId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val resource = createRdfResource()

        every { thingRepository.findAll(any(), uri = any()) } returns pageOf(createResource())

        rdfResourceProcessor.beforeStep(stepExecution)
        rdfResourceProcessor.process(resource) shouldBe null
        rdfResourceProcessor.afterStep(stepExecution)

        resourceIriToId.size shouldBe 0

        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(resource.iri.toString())) }
    }
}
