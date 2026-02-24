package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRepresentationAdapter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val COMPARISON_JSON_V2 = "application/vnd.orkg.comparison.v2+json"

@RestController
@RequestMapping("/api/comparisons")
class ComparisonControllerV2 : ComparisonRepresentationAdapter {
    @GetMapping("/{id}", produces = [COMPARISON_JSON_V2])
    fun findById(
        @PathVariable id: ThingId,
    ): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()

    @GetMapping(produces = [COMPARISON_JSON_V2])
    fun findAll(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()

    @RequireLogin
    @PostMapping(consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()

    @RequireLogin
    @PutMapping("/{id}", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun update(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()
}
