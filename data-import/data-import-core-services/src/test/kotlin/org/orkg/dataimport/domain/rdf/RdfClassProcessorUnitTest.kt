package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createRdfClass
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.pageOf

internal class RdfClassProcessorUnitTest : MockkBaseTest {
    private val contributorId = ContributorId("112f3395-4008-4ce4-b6e4-973fbc1b25e2")
    private val extractionMethod = ExtractionMethod.MANUAL
    private val modifiable = true
    private val thingRepository: ThingRepository = mockk()
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()

    private val rdfClassProcessor = RdfClassProcessor(contributorId, extractionMethod, modifiable, thingRepository, unsafeClassUseCases)

    @Test
    fun `Given an rdf class, when processing and class does not already exist, it creates a new class and saves the class iri to id mapping to the job execution context`() {
        val classIriToId = mutableMapOf<String, ThingId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val `class` = createRdfClass()
        val id = ThingId("C123")

        every { thingRepository.findAll(any(), uri = any()) } returns pageOf()
        every { unsafeClassUseCases.create(any()) } returns id

        rdfClassProcessor.beforeStep(stepExecution)
        rdfClassProcessor.process(`class`) shouldBe null
        rdfClassProcessor.afterStep(stepExecution)

        classIriToId.size shouldBe 1
        classIriToId shouldContainExactly mapOf(
            `class`.iri.toString() to id,
        )

        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(`class`.iri.toString())) }
        verify(exactly = 1) {
            unsafeClassUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.label shouldBe `class`.label
                    it.uri shouldBe IRI.create(`class`.iri.toString())
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given an rdf class, when processing and class already exists, it does not create a new class`() {
        val classIriToId = mutableMapOf<String, ThingId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val `class` = createRdfClass()

        every { thingRepository.findAll(any(), uri = any()) } returns pageOf(createClass())

        rdfClassProcessor.beforeStep(stepExecution)
        rdfClassProcessor.process(`class`) shouldBe null
        rdfClassProcessor.afterStep(stepExecution)

        classIriToId.size shouldBe 0

        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(`class`.iri.toString())) }
    }
}
