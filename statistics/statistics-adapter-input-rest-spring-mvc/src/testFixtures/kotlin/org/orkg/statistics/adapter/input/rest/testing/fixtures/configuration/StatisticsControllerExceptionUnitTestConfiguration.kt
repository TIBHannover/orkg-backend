package org.orkg.statistics.adapter.input.rest.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.json.CommonJacksonModule
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
)
@TestConfiguration
class StatisticsControllerExceptionUnitTestConfiguration
