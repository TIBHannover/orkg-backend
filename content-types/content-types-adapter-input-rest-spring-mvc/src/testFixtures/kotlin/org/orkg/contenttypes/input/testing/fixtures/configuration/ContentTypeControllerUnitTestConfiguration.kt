package org.orkg.contenttypes.input.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.input.testing.fixtures.ContentTypeDocumentationContextProvider
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.adapter.input.rest.testing.fixtures.GraphDocumentationContextProvider
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    ExceptionTestConfiguration::class,
    FixedClockConfig::class,
    WebMvcConfiguration::class,
    CommonJacksonModule::class,
    GraphJacksonModule::class,
    ContentTypeJacksonModule::class,
    CommonDocumentationContextProvider::class,
    GraphDocumentationContextProvider::class,
    ContentTypeDocumentationContextProvider::class,
)
@TestConfiguration
class ContentTypeControllerUnitTestConfiguration
