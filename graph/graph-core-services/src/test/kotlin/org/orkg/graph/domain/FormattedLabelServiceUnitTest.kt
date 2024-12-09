package org.orkg.graph.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.fixtures.createResource

internal class FormattedLabelServiceUnitTest {

    private val repository: FormattedLabelRepository = mockk()
    private val service = FormattedLabelService(repository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(repository)
    }

    @Test
    fun `given a list of resources, when fetching formatted labels, then formatted labels are returned`() {
        val resources = listOf(
            createResource(id = ThingId("R1"), classes = setOf(Classes.paper)),
            createResource(id = ThingId("R2"), classes = setOf(Classes.comparison)),
            createResource(id = ThingId("R3"))
        )

        val templatedResources = listOf(
            TemplatedResource(
                id = ThingId("R1"),
                templateId = ThingId("T1"),
                label = "template T1",
                classId = Classes.paper.value,
                format = "some {P32}",
                predicates = listOf("P32"),
                values = listOf("paper")
            ),
            TemplatedResource(
                id = ThingId("R2"),
                templateId = ThingId("T2"),
                label = "template T2",
                classId = Classes.comparison.value,
                format = "{P1} text {P2}",
                predicates = listOf("P1", "P2"),
                values = listOf("this", "is formatted")
            )
        )
        every { repository.findTemplateSpecs(any()) } returns templatedResources.associateBy { it.id }

        service.findFormattedLabels(resources).asClue {
            it.size shouldBe 3
            it shouldBe mapOf(
                ThingId("R1") to FormattedLabel.of("some paper"),
                ThingId("R2") to FormattedLabel.of("this text is formatted"),
                ThingId("R3") to null
            )
        }

        verify(exactly = 1) {
            repository.findTemplateSpecs(
                mapOf(
                    ThingId("R1") to Classes.paper,
                    ThingId("R2") to Classes.comparison
                )
            )
        }
    }

    @Test
    fun `given a list of resources without classes, when fetching formatted labels, then it does not query the repository and formatted labels are returned`() {
        val resources = listOf(
            createResource(id = ThingId("R1")),
            createResource(id = ThingId("R2"))
        )

        service.findFormattedLabels(resources) shouldBe resources.associate { it.id to null }

        verify(exactly = 0) { repository.findTemplateSpecs(any()) }
    }

    @Test
    fun `given an empty list of resources, when fetching formatted labels, then it does not query the repository and empty labels are returned`() {
        service.findFormattedLabels(emptyList()) shouldBe emptyMap()

        verify(exactly = 0) { repository.findTemplateSpecs(any()) }
    }
}
