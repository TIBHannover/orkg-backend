package org.orkg.core.application

import org.springframework.boot.info.BuildProperties
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/version", produces = [MediaType.APPLICATION_JSON_VALUE])
class VersionController(
    private val buildProperties: BuildProperties,
) {
    @GetMapping
    fun version(): Map<String, String?> = mapOf("version" to buildProperties.version)
}
