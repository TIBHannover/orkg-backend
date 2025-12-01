package org.orkg.dataimport.testing.fixtures.configuration

import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.json.CommonJacksonModule
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.testing.fixtures.DataImportDocumentationContextProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    CommonJacksonModule::class,
    CommonDocumentationContextProvider::class,
    DataImportJacksonModule::class,
    DataImportDocumentationContextProvider::class,
)
@TestConfiguration
class DataImportControllerExceptionUnitTestConfiguration
