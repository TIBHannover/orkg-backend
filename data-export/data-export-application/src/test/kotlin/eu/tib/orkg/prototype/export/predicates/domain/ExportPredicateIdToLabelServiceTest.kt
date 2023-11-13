package eu.tib.orkg.prototype.export.predicates.domain

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.export.shared.domain.FileExportService
import eu.tib.orkg.prototype.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createPredicate
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ExportPredicateIdToLabelServiceTest : DescribeSpec({
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
