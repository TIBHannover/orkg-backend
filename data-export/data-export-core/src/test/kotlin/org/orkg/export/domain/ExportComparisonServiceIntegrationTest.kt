package org.orkg.export.domain

import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.common.testing.fixtures.MockkDescribeSpec
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty
import org.orkg.testing.pageOf
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.json.JsonMapper

internal class ExportComparisonServiceIntegrationTest :
    MockkDescribeSpec({
        val comparisonService: ComparisonUseCases = mockk()
        val comparisonRepository: ComparisonRepository = mockk()
        val fileExportService = FileExportService()
        val objectMapper = JsonMapper.builder()
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build()
        val service = ExportComparisonService(
            comparisonService = comparisonService,
            comparisonRepository = comparisonRepository,
            fileExportService = fileExportService,
            objectMapper = objectMapper,
            comparisonPublishBaseUri = "https://orkg.org/comparison/"
        )

        val targetDir = tempdir()

        fun String.toJsonL(): String = objectMapper.writeValueAsString(objectMapper.readTree(this)) + "\n"

        context("dumping to default location") {
            verifyThatDirectoryExistsAndIsEmpty(targetDir)

            val targetFile = targetDir.resolve("test-export.jsonl")
            targetFile.exists() shouldBe false

            val comparison = createComparison()

            every { comparisonService.findAllCurrentAndListedAndUnpublishedComparisons(any()) } returns pageOf(comparison, comparison)
            every { comparisonRepository.findAllDOIsRelatedToComparison(comparison.id) } returns listOf("test/doi")

            withContext(Dispatchers.IO) {
                service.export(targetFile.absolutePath)
            }

            val responseJsonL = requestJson("datacite/registerComparisonDoi").toJsonL()

            it("writes the correct result") {
                targetFile.exists() shouldBe true
                targetFile.readText() shouldBe (responseJsonL + responseJsonL)
                verify(exactly = 1) { comparisonService.findAllCurrentAndListedAndUnpublishedComparisons(any()) }
                verify(exactly = 2) { comparisonRepository.findAllDOIsRelatedToComparison(comparison.id) }
            }
        }
    })
