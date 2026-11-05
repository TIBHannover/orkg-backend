package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.IRI
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.UUID

open class RdfPredicateParser(
    private val rdfFileId: UUID,
    private val rdfLabelResolverService: RdfLabelResolverService,
) : ItemProcessor<IRI, RdfPredicate> {
    override fun process(iri: IRI): RdfPredicate? {
        if (iri.toString().startsWith(ORKG_VOCAB_PREFIX)) {
            return null
        }
        return RdfPredicate(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            iri = iri,
            label = rdfLabelResolverService.resolveLabel(rdfFileId, iri),
        )
    }
}
