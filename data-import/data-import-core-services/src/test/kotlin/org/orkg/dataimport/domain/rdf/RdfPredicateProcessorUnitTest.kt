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
import org.orkg.dataimport.domain.RDF_PREDICATE_IRI_TO_ID
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createRdfPredicate
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.pageOf

internal class RdfPredicateProcessorUnitTest : MockkBaseTest {
    private val contributorId = ContributorId("112f3395-4008-4ce4-b6e4-973fbc1b25e2")
    private val extractionMethod = ExtractionMethod.MANUAL
    private val modifiable = true
    private val thingRepository: ThingRepository = mockk()
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()

    private val rdfPredicateProcessor = RdfPredicateProcessor(
        contributorId,
        extractionMethod,
        modifiable,
        thingRepository,
        unsafePredicateUseCases,
    )

    @Test
    fun `Given an rdf predicate, when processing and predicate does not already exist, it creates a new predicate and saves the predicate iri to id mapping to the job execution context`() {
        val predicateIriToId = mutableMapOf<String, ThingId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val predicate = createRdfPredicate()
        val id = ThingId("P123")

        every { thingRepository.findAll(any(), uri = any()) } returns pageOf()
        every { unsafePredicateUseCases.create(any()) } returns id

        rdfPredicateProcessor.beforeStep(stepExecution)
        rdfPredicateProcessor.process(predicate) shouldBe null
        rdfPredicateProcessor.afterStep(stepExecution)

        predicateIriToId.size shouldBe 1
        predicateIriToId shouldContainExactly mapOf(
            predicate.iri.toString() to id,
        )

        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(predicate.iri.toString())) }
        verify(exactly = 1) {
            unsafePredicateUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.label shouldBe predicate.label
                    it.uri shouldBe IRI.create(predicate.iri.toString())
                    it.extractionMethod shouldBe extractionMethod
                    it.modifiable shouldBe modifiable
                },
            )
        }
    }

    @Test
    fun `Given an rdf predicate, when processing and predicate already exists, it does not create a new predicate`() {
        val predicateIriToId = mutableMapOf<String, ThingId>()
        val jobExecution = createJobExecution().apply {
            executionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val predicate = createRdfPredicate()

        every { thingRepository.findAll(any(), uri = any()) } returns pageOf(createPredicate())

        rdfPredicateProcessor.beforeStep(stepExecution)
        rdfPredicateProcessor.process(predicate) shouldBe null
        rdfPredicateProcessor.afterStep(stepExecution)

        predicateIriToId.size shouldBe 0

        verify(exactly = 1) { thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(predicate.iri.toString())) }
    }
}
