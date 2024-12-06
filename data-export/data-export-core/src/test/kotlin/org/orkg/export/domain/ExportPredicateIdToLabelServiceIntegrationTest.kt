package org.orkg.export.domain

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orkg.common.ThingId
import org.orkg.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.pageOf

internal class ExportPredicateIdToLabelServiceIntegrationTest : DescribeSpec({
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

        val predicate1 = createPredicate(id = ThingId("P1"), label = "label1")
        val predicate2 = createPredicate(id = ThingId("P2"), label = "label2")

        every { predicateRepository.findAll(any()) } returns pageOf(predicate1, predicate2)

        withContext(Dispatchers.IO) {
            service.export(targetFile.absolutePath)
        }

        it("writes the correct result") {
            targetFile.exists() shouldBe true
            targetFile.readText() shouldBe """{"P1":"label1","P2":"label2"}"""
        }
    }
}) {
    override fun isolationMode() = IsolationMode.InstancePerLeaf
}
