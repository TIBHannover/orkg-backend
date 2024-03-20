package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import java.net.URI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.graph.domain.InvalidLabel

class PublicationInfoValidatorUnitTest {
    private val publicationInfoValidator = PublicationInfoValidator<PublicationInfoDefinition, Unit> { it }

    @Test
    fun `Given a publication info definition, when valid, it returns success`() {
        publicationInfoValidator(
            PublicationInfoDefinition(
                publishedMonth = 1,
                publishedYear = 2024,
                publishedIn = "valid venue",
                url = URI.create("https://orkg.org/paper/R1000")
            ),
            Unit
        )
    }

    @Test
    fun `Given a publication info definition, when venue label is invalid, it throws an exception`() {
        assertThrows<InvalidLabel> {
            publicationInfoValidator(
                PublicationInfoDefinition(
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
    fun `Given a publication info definition, when publication month is invalid, it throws an exception`() {
        assertThrows<InvalidMonth> {
            publicationInfoValidator(
                PublicationInfoDefinition(
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
