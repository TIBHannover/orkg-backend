package org.orkg.dataimport.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.json.CommonJacksonModule
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.testing.fixtures.DataImportDocumentationContextProvider
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    ExceptionTestConfiguration::class,
    FixedClockConfig::class,
    WebMvcConfiguration::class,
    CommonJacksonModule::class,
    DataImportJacksonModule::class,
    CommonDocumentationContextProvider::class,
    DataImportDocumentationContextProvider::class,
)
@TestConfiguration
class DataImportControllerUnitTestConfiguration
