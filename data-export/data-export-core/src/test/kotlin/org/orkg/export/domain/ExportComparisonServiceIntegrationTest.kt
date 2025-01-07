package org.orkg.export.domain

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orkg.common.testing.fixtures.MockkDescribeSpec
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty
import org.orkg.testing.pageOf

internal class ExportComparisonServiceIntegrationTest : MockkDescribeSpec({
    val comparisonService: ComparisonUseCases = mockk()
    val comparisonRepository: ComparisonRepository = mockk()
    val fileExportService = FileExportService()
    val objectMapper = ObjectMapper()
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

        it("writes the correct result") {
            targetFile.exists() shouldBe true
            targetFile.readText() shouldBe (dummyComparisonDataCiteJson.toJsonL() + dummyComparisonDataCiteJson.toJsonL())
            verify(exactly = 1) { comparisonService.findAllCurrentAndListedAndUnpublishedComparisons(any()) }
            verify(exactly = 2) { comparisonRepository.findAllDOIsRelatedToComparison(comparison.id) }
        }
    }
})

private const val dummyComparisonDataCiteJson = """
{
  "data": {
    "attributes": {
      "creators": [
        {
          "name": "Josiah Stinkney Carberry",
          "nameIdentifiers": [
            {
              "schemeUri": "https://orcid.org",
              "nameIdentifier": "https://orcid.org/0000-0002-1825-0097",
              "nameIdentifierScheme": "ORCID"
            }
          ],
          "nameType": "Personal"
        },
        {
          "name": "Author 2",
          "nameIdentifiers": [
            
          ],
          "nameType": "Personal"
        }
      ],
      "titles": [
        {
          "title": "Dummy Comparison Title",
          "lang": "en"
        }
      ],
      "publicationYear": 2023,
      "subjects": [
        {
          "subject": "Research Field 1",
          "lang": "en"
        },
        {
          "subject": "Research Field 2",
          "lang": "en"
        }
      ],
      "types": {
        "resourceType": "Comparison",
        "resourceTypeGeneral": "Dataset"
      },
      "relatedIdentifiers": [
        {
          "relatedIdentifier": "https://doi.org/test/doi",
          "relatedIdentifierType": "DOI",
          "relationType": "IsVariantFormOf"
        }
      ],
      "rightsList": [
        {
          "rights": "Creative Commons Attribution-ShareAlike 4.0 International License.",
          "rightsUri": "https://creativecommons.org/licenses/by-sa/4.0/"
        }
      ],
      "descriptions": [
        {
          "description": "Some description about the contents",
          "descriptionType": "Abstract"
        }
      ],
      "url": "https://orkg.org/comparison/R8186",
      "language": "en",
      "publisher": "Open Research Knowledge Graph"
    },
    "type": "dois"
  }
}
"""
