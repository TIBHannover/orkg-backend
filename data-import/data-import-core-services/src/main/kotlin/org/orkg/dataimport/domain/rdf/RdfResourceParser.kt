package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.orkg.dataimport.output.RdfTripleRepository
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.UUID

open class RdfResourceParser(
    private val rdfFileId: UUID,
    private val rdfTripleRepository: RdfTripleRepository,
) : ItemProcessor<Resource, RdfResource> {
    override fun process(iri: Resource): RdfResource? {
        if (iri.toString().startsWith(ORKG_VOCAB_PREFIX)) {
            return null
        }
        return RdfResource(
            id = UUID.randomUUID(),
            rdfFileId = rdfFileId,
            iri = iri,
            label = rdfTripleRepository.findLabel(rdfFileId, iri),
            classes = rdfTripleRepository.findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(
                rdfFileId = rdfFileId,
                subject = iri,
                predicate = RDF.TYPE,
            ),
        )
    }
}
