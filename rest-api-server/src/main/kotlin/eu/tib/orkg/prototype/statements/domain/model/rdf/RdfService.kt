package eu.tib.orkg.prototype.statements.domain.model.rdf

import java.io.OutputStream

interface RdfService {
    fun dumpToNTriple(out: OutputStream)
}
