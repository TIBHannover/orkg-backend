package org.orkg.export.input

import java.io.Writer

interface ExportPredicateIdToLabelUseCase {
    fun export(writer: Writer)
    fun export(path: String?)
}
