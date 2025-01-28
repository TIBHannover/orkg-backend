package org.orkg.statistics.adapter.input.rest

import org.orkg.statistics.adapter.input.rest.mapping.MetricRepresentationAdapter
import org.orkg.statistics.input.RetrieveStatisticsUseCase
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/statistics", produces = [MediaType.APPLICATION_JSON_VALUE])
class StatisticsController(private val service: RetrieveStatisticsUseCase) : MetricRepresentationAdapter {
    @GetMapping
    fun findAllGroups(uriComponentsBuilder: UriComponentsBuilder): Map<String, EndpointReference> =
        service.findAllGroups().associateWith {
            EndpointReference(
                uriComponentsBuilder.cloneBuilder()
                    .path("/api/statistics/{group}")
                    .buildAndExpand(it)
                    .toUri()
            )
        }

    @GetMapping("/{group}")
    fun findAllMetricsByGroup(
        @PathVariable group: String,
        uriComponentsBuilder: UriComponentsBuilder
    ): Map<String, EndpointReference> =
        service.findAllMetricsByGroup(group).associate {
            it.name to EndpointReference(
                uriComponentsBuilder.cloneBuilder()
                    .path("/api/statistics/{group}/{metric}")
                    .buildAndExpand(it.group, it.name)
                    .toUri()
            )
        }

    @GetMapping("/{group}/{name}")
    fun findMetricByGroupAndName(
        @PathVariable group: String,
        @PathVariable name: String,
        @RequestParam parameters: Map<String, String>
    ): MetricRepresentation =
        service.findMetricByGroupAndName(group, name)
            .let { metric -> metric.toMetricRepresentation(metric.value(parameters)) }
}
