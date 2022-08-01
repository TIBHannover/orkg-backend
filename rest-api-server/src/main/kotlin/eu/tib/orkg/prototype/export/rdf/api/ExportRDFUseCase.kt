package eu.tib.orkg.prototype.export.rdf.api

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.eclipse.rdf4j.model.Model

interface ExportRDFUseCase {
    fun dumpToNTriple(): String
    fun rdfModelFor(id: ClassId): Optional<Model>
    fun rdfModelFor(id: PredicateId): Optional<Model>
    fun rdfModelFor(id: ResourceId): Optional<Model>
}
