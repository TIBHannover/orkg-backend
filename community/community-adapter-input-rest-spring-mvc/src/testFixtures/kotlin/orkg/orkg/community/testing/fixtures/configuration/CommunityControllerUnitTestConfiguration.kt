package orkg.orkg.community.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import orkg.orkg.community.testing.fixtures.CommunityDocumentationContextProvider

@Import(
    ExceptionTestConfiguration::class,
    FixedClockConfig::class,
    WebMvcConfiguration::class,
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
    CommunityJacksonModule::class,
    CommunityDocumentationContextProvider::class,
)
@TestConfiguration
class CommunityControllerUnitTestConfiguration
