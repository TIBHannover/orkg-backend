package org.orkg.widget.adapter.input.rest

import org.orkg.widget.domain.WidgetServiceUnavailable
import org.orkg.widget.input.ResolveDOIUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/widgets", produces = [MediaType.APPLICATION_JSON_VALUE])
class WidgetController(
    private val resolveDOIUseCase: ResolveDOIUseCase,
    @param:Value($$"${orkg.widget.enabled:#{true}}")
    private val enabled: Boolean,
) {
    @GetMapping
    fun searchDoi(
        @RequestParam(required = false) doi: String?,
        @RequestParam(required = false) title: String?,
    ): ResolveDOIUseCase.WidgetInfo =
        if (enabled) {
            resolveDOIUseCase.resolveDOI(doi, title)
        } else {
            throw WidgetServiceUnavailable()
        }
}
