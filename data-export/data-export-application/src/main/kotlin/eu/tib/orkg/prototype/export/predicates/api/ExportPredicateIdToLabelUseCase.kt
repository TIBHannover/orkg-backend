package eu.tib.orkg.prototype.export.predicates.api

import java.io.Writer

interface ExportPredicateIdToLabelUseCase {
    fun export(writer: Writer)
    fun export(path: String?)
}
