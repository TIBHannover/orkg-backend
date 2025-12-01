package org.orkg.notifications.adapter.input.rest.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    ExceptionTestConfiguration::class,
    FixedClockConfig::class,
    CommonDocumentationContextProvider::class,
)
@TestConfiguration
class DataImportControllerUnitTestConfiguration
