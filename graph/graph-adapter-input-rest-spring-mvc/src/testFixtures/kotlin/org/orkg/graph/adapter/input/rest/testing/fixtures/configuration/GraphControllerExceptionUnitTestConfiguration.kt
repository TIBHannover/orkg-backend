package org.orkg.graph.adapter.input.rest.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.adapter.input.rest.testing.fixtures.GraphDocumentationContextProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
    GraphJacksonModule::class,
    GraphDocumentationContextProvider::class,
)
@TestConfiguration
class GraphControllerExceptionUnitTestConfiguration
