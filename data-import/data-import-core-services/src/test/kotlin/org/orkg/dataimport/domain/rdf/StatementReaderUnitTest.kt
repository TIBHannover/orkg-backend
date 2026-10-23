package org.orkg.dataimport.domain.rdf

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.testing.fixtures.createRdfFile
import org.orkg.dataimport.output.RdfFileRepository
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.item.ItemStreamException
import java.util.Optional
import java.util.UUID

internal class StatementReaderUnitTest : MockkBaseTest {
    private val rdfFileId = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
    private val rdfFileRepository: RdfFileRepository = mockk()

    private val statementReader = StatementReader(rdfFileId, rdfFileRepository)

    @Test
    fun `Given an rdf file id, when reading statements, it returns the correct result`() {
        val rdfFile = createRdfFile()

        every { rdfFileRepository.findById(rdfFileId) } returns Optional.of(rdfFile)

        statementReader.open(ExecutionContext())
        statementReader.read().asClue {
            it.shouldNotBeNull()
            it.shouldBeInstanceOf<Statement>()
            it.subject shouldBe SimpleValueFactory.getInstance().createIRI("http://example.org/Alice")
            it.predicate shouldBe RDF.TYPE
            it.`object` shouldBe SimpleValueFactory.getInstance().createIRI("http://xmlns.com/foaf/0.1/Person")
            it.context shouldBe null
        }
        statementReader.close()

        verify(exactly = 1) { rdfFileRepository.findById(rdfFileId) }
    }

    @Test
    fun `Given an rdf file id, when reading statements and rdf file does not exist, it throws an exception`() {
        every { rdfFileRepository.findById(rdfFileId) } returns Optional.empty()

        shouldThrow<ItemStreamException> { statementReader.open(ExecutionContext()) }

        verify(exactly = 1) { rdfFileRepository.findById(rdfFileId) }
    }

    @Test
    fun `Given an rdf file id, when reading statements and rdf file contains malformed data, it throws an exception`() {
        val rdfFile = createRdfFile(data = "error")

        every { rdfFileRepository.findById(rdfFileId) } returns Optional.of(rdfFile)

        shouldThrow<ItemStreamException> { statementReader.open(ExecutionContext()) }

        verify(exactly = 1) { rdfFileRepository.findById(rdfFileId) }
    }
}
