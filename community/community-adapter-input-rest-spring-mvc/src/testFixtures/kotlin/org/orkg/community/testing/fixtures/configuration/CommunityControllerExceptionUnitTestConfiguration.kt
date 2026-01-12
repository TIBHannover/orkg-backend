package org.orkg.community.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.testing.fixtures.CommunityDocumentationContextProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
    CommunityJacksonModule::class,
    CommunityDocumentationContextProvider::class,
)
@TestConfiguration
class CommunityControllerExceptionUnitTestConfiguration
