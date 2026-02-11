package org.orkg.export.domain

import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orkg.common.testing.fixtures.MockkDescribeSpec
import org.orkg.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.pageOf
import tools.jackson.databind.ObjectMapper

internal class ExportPredicateIdToLabelServiceIntegrationTest :
    MockkDescribeSpec({
        val predicateRepository: PredicateRepository = mockk()
        val fileExportService = FileExportService()
        val objectMapper = ObjectMapper()
        val service = ExportPredicateIdToLabelService(
            predicateRepository = predicateRepository,
            fileExportService = fileExportService,
            objectMapper = objectMapper
        )

        val targetDir = tempdir()

        context("dumping to default location") {
            verifyThatDirectoryExistsAndIsEmpty(targetDir)

            val targetFile = targetDir.resolve("test-export.json")
            targetFile.exists() shouldBe false

            val predicate1 = createPredicate(id = Predicates.yields, label = "label1")
            val predicate2 = createPredicate(id = Predicates.employs, label = "label2")

            every { predicateRepository.findAll(any()) } returns pageOf(predicate1, predicate2)

            withContext(Dispatchers.IO) {
                service.export(targetFile.absolutePath)
            }

            it("writes the correct result") {
                targetFile.exists() shouldBe true
                targetFile.readText() shouldBe """{"P1":"label1","P2":"label2"}"""
                verify(exactly = 1) { predicateRepository.findAll(any()) }
            }
        }
    })
