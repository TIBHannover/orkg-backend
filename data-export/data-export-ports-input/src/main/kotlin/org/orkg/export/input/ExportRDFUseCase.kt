package org.orkg.export.input

import org.eclipse.rdf4j.model.Model
import org.orkg.common.ThingId
import java.io.Writer
import java.util.Optional

interface ExportRDFUseCase {
    fun dumpToNTriple(writer: Writer)

    fun dumpToNTriple(path: String?)

    fun rdfModelForClass(id: ThingId): Optional<Model>

    fun rdfModelForPredicate(id: ThingId): Optional<Model>

    fun rdfModelForResource(id: ThingId): Optional<Model>
}
