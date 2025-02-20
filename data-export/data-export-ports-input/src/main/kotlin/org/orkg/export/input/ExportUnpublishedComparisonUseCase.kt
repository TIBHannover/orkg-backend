package org.orkg.export.input

import java.io.Writer

interface ExportUnpublishedComparisonUseCase {
    fun export(writer: Writer)

    fun export(path: String?)
}
