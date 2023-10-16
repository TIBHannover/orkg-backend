package eu.tib.orkg.prototype.export.comparisons.api

import java.io.Writer

interface ExportUnpublishedComparisonUseCase {
    fun export(writer: Writer)
    fun export(path: String?)
}
