package org.orkg.widget.adapter.input.rest

import org.orkg.widget.input.ResolveDOIUseCase
import org.orkg.widget.input.ResolveDOIUseCase.WidgetInfo
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/widgets/", produces = [MediaType.APPLICATION_JSON_VALUE])
class WidgetController(
    private val resolveDOIUseCase: ResolveDOIUseCase,
) {
    @GetMapping("/")
    fun searchDoi(
        @RequestParam(required = false) doi: String?,
        @RequestParam(required = false) title: String?
    ): WidgetInfo = resolveDOIUseCase.resolveDOI(doi, title)
}
