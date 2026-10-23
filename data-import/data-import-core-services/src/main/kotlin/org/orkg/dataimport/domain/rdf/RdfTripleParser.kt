package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Statement
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.UUID

open class RdfTripleParser(
    private val rdfFileId: UUID,
) : ItemProcessor<Statement, RdfTriple> {
    override fun process(item: Statement): RdfTriple? =
        RdfTriple(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            subject = item.subject,
            predicate = item.predicate,
            `object` = item.`object`,
        )
}
