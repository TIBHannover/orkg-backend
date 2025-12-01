package org.orkg.contenttypes.input.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.input.testing.fixtures.ContentTypeDocumentationContextProvider
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.adapter.input.rest.testing.fixtures.GraphDocumentationContextProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
    GraphJacksonModule::class,
    GraphDocumentationContextProvider::class,
    ContentTypeJacksonModule::class,
    ContentTypeDocumentationContextProvider::class,
)
@TestConfiguration
class ContentTypeControllerExceptionUnitTestConfiguration
