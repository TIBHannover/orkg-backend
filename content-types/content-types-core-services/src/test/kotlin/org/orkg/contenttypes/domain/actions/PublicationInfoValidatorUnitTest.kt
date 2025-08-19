package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.graph.domain.InvalidLabel

internal class PublicationInfoValidatorUnitTest {
    private val publicationInfoValidator = PublicationInfoValidator<PublicationInfoCommand, Unit> { it }

    @Test
    fun `Given a publication info command, when valid, it returns success`() {
        publicationInfoValidator(
            PublicationInfoCommand(
                publishedMonth = 1,
                publishedYear = 2024,
                publishedIn = "valid venue",
                url = ParsedIRI.create("https://orkg.org/paper/R1000")
            ),
            Unit
        )
    }

    @Test
    fun `Given a publication info command, when venue label is invalid, it throws an exception`() {
        assertThrows<InvalidLabel> {
            publicationInfoValidator(
                PublicationInfoCommand(
                    publishedMonth = null,
                    publishedYear = null,
                    publishedIn = "\n",
                    url = null
                ),
                Unit
            )
        }.asClue {
            it.property shouldBe "published_in"
        }
    }

    @Test
    fun `Given a publication info command, when publication month is invalid, it throws an exception`() {
        assertThrows<InvalidMonth> {
            publicationInfoValidator(
                PublicationInfoCommand(
                    publishedMonth = 13,
                    publishedYear = null,
                    publishedIn = null,
                    url = null
                ),
                Unit
            )
        }
    }
}
