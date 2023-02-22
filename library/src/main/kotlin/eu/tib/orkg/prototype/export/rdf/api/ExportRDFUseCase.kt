package eu.tib.orkg.prototype.export.rdf.api

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.eclipse.rdf4j.model.Model

interface ExportRDFUseCase {
    fun dumpToNTriple(): String
    fun rdfModelForClass(id: ThingId): Optional<Model>
    fun rdfModelForPredicate(id: ThingId): Optional<Model>
    fun rdfModelForResource(id: ThingId): Optional<Model>
}
