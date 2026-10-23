package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.IRI
import org.orkg.dataimport.output.RdfTripleRepository
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.UUID

open class RdfPredicateParser(
    private val rdfFileId: UUID,
    private val rdfTripleRepository: RdfTripleRepository,
) : ItemProcessor<IRI, RdfPredicate> {
    override fun process(iri: IRI): RdfPredicate? {
        if (iri.toString().startsWith(ORKG_VOCAB_PREFIX)) {
            return null
        }
        return RdfPredicate(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            iri = iri,
            label = rdfTripleRepository.findLabel(rdfFileId, iri),
        )
    }
}
