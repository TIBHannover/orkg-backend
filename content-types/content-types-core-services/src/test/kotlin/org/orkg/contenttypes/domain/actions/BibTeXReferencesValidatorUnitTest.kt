package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbibtex.BibTeXDatabase
import org.jbibtex.BibTeXParser
import org.jbibtex.ParseException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.InvalidBibTeXReference
import java.io.Reader
import java.io.StringWriter

private const val REFERENCE1 = "@misc{R50008,\ttitle = {Data integration and disintegration: Managing {Springer} {Nature} {SciGraph} with {SHACL} and {OWL}},}"
private const val REFERENCE2 = "@misc{R50006,\ttitle = {Papers with code},}"

internal class BibTeXReferencesValidatorUnitTest : MockkBaseTest {
    private val parser: BibTeXParser = mockk()

    private val bibTeXReferencesValidator = BibTeXReferencesValidator<List<String>?, List<String>>({ it }, { it }, parser)

    @Test
    fun `Given a list of bibtex references, when validating, it returns success`() {
        every { parser.parse(any()) } returns BibTeXDatabase()

        bibTeXReferencesValidator(listOf(REFERENCE1), emptyList())

        verify(exactly = 1) {
            parser.parse(
                withArg {
                    it.contentAsString shouldBe REFERENCE1
                }
            )
        }
    }

    @Test
    fun `Given a list of bibtex references, when bibtex reference is invalid, it throws an exception`() {
        every { parser.parse(any()) } throws ParseException()

        assertThrows<InvalidBibTeXReference> { bibTeXReferencesValidator(listOf(REFERENCE1), emptyList()) }

        verify(exactly = 1) {
            parser.parse(
                withArg {
                    it.contentAsString shouldBe REFERENCE1
                }
            )
        }
    }

    @Test
    fun `Given a list of bibtex references, when old list of bibtex references is identical, it does nothing`() {
        val ids = listOf(REFERENCE1, REFERENCE2)
        bibTeXReferencesValidator(ids, ids)
    }

    @Test
    fun `Given a list of bibtex references, when no new bibtex references list is set, it does nothing`() {
        val ids = listOf(REFERENCE1, REFERENCE2)
        bibTeXReferencesValidator(null, ids)
    }

    private val Reader.contentAsString: String
        get() {
            reset()
            val writer = StringWriter()
            transferTo(writer)
            return writer.toString()
        }
}
