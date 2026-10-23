package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.ParseErrorListener
import org.eclipse.rdf4j.rio.ParserConfig
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings
import org.orkg.dataimport.domain.RdfFileNotFound
import org.orkg.dataimport.domain.RdfParsingException
import org.orkg.dataimport.domain.internal.RecordParsingException
import org.orkg.dataimport.output.RdfFileRepository
import org.springframework.batch.infrastructure.item.support.AbstractItemCountingItemStreamItemReader
import java.io.StringReader
import java.util.UUID

open class StatementReader(
    private val rdfFileId: UUID,
    private val rdfFileRepository: RdfFileRepository,
) : AbstractItemCountingItemStreamItemReader<Statement>() {
    private lateinit var model: Model
    private lateinit var iterator: Iterator<Statement>

    override fun doRead(): Statement? {
        if (iterator.hasNext()) {
            return iterator.next()
        }
        return null
    }

    override fun doOpen() {
        val rdfFile = rdfFileRepository.findById(rdfFileId).orElseThrow { RdfFileNotFound(rdfFileId) }
        val errorListener = ParseErrorCollector()
        val parserConfig = ParserConfig()
            .set(BasicParserSettings.PRESERVE_BNODE_IDS, true)
            .set(
                JSONLDSettings.WHITELIST,
                JSONLDSettings.WHITELIST.defaultValue + "https://w3id.org/ro/crate/1.1/context",
            )
        try {
            model = Rio.parse(
                StringReader(rdfFile.data),
                rdfFile.baseUri?.toString(),
                rdfFile.format.rdfFormat,
                parserConfig,
                SimpleValueFactory.getInstance(),
                errorListener,
            )
        } catch (t: Throwable) {
            throw RecordParsingException(listOf(t))
        }
        if (errorListener.hasErrors) {
            model.clear()
            throw RecordParsingException(errorListener.allErrors)
        }
        iterator = model.iterator()
    }

    override fun doClose() {
        model.clear()
    }

    class ParseErrorCollector : ParseErrorListener {
        val warnings = mutableListOf<RdfParsingException>()
        val errors = mutableListOf<RdfParsingException>()
        val fatalErrors = mutableListOf<RdfParsingException>()

        override fun warning(msg: String, lineNo: Long, colNo: Long) {
            warnings += RdfParsingException(msg, lineNo, colNo)
        }

        override fun error(msg: String, lineNo: Long, colNo: Long) {
            errors += RdfParsingException(msg, lineNo, colNo)
        }

        override fun fatalError(msg: String, lineNo: Long, colNo: Long) {
            fatalErrors += RdfParsingException(msg, lineNo, colNo)
        }

        val hasErrors: Boolean get() =
            errors.isNotEmpty() || fatalErrors.isNotEmpty()

        val allErrors: List<RdfParsingException> get() =
            errors + fatalErrors
    }
}
