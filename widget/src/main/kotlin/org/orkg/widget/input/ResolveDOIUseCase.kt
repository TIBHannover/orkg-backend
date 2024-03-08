package org.orkg.widget.input

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId

interface ResolveDOIUseCase {
    fun resolveDOI(doi: String?, title: String?): WidgetInfo

    data class WidgetInfo(
        val id: ThingId,
        val doi: String?,
        val title: String,
        @JsonProperty("num_statements")
        val numberOfStatements: Long,
        val `class`: String,
    )
}
