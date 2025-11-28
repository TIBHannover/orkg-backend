package org.orkg.export.adapter.input.rest.testing.fixtures.configuration

import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    ExceptionTestConfiguration::class,
    FixedClockConfig::class,
    WebMvcConfiguration::class,
)
@TestConfiguration
class DataExportControllerUnitTestConfiguration
