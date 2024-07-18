package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.Reader
import java.io.StringWriter
import org.jbibtex.BibTeXDatabase
import org.jbibtex.BibTeXParser
import org.jbibtex.ParseException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.InvalidBibTeXReference

private const val reference1 = "@misc{R50008,\ttitle = {Data integration and disintegration: Managing {Springer} {Nature} {SciGraph} with {SHACL} and {OWL}},}"
private const val reference2 = "@misc{R50006,\ttitle = {Papers with code},}"

class BibTeXReferencesValidatorUnitTest {
    private val parser: BibTeXParser = mockk()

    private val bibTeXReferencesValidator = BibTeXReferencesValidator<List<String>?, List<String>>({ it }, { it }, parser)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(parser)
    }

    @Test
    fun `Given a list of bibtex references, when validating, it returns success`() {
        every { parser.parse(any()) } returns BibTeXDatabase()

        bibTeXReferencesValidator(listOf(reference1), emptyList())

        verify(exactly = 1) {
            parser.parse(withArg {
                it.contentAsString shouldBe reference1
            })
        }
    }

    @Test
    fun `Given a list of bibtex references, when bibtex reference is invalid, it throws an exception`() {
        every { parser.parse(any()) } throws ParseException()

        assertThrows<InvalidBibTeXReference> { bibTeXReferencesValidator(listOf(reference1), emptyList()) }

        verify(exactly = 1) {
            parser.parse(withArg {
                it.contentAsString shouldBe reference1
            })
        }
    }

    @Test
    fun `Given a list of bibtex references, when old list of bibtex references is identical, it does nothing`() {
        val ids = listOf(reference1, reference2)
        bibTeXReferencesValidator(ids, ids)
    }

    @Test
    fun `Given a list of bibtex references, when no new bibtex references list is set, it does nothing`() {
        val ids = listOf(reference1, reference2)
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
