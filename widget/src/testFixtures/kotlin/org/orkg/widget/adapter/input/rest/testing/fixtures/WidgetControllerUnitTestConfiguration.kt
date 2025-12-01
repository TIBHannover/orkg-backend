package org.orkg.widget.adapter.input.rest.testing.fixtures

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.json.CommonJacksonModule
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    ExceptionTestConfiguration::class,
    FixedClockConfig::class,
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
)
@TestConfiguration
class WidgetControllerUnitTestConfiguration
