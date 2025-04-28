package org.orkg.notifications.domain.internal

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest

internal class MessageFormatterUnitTest : MockkBaseTest {
    private val messages: Map<String, String> = mockk()

    private val messageFormatter = MessageFormatter(messages, ::escapeHtml4)

    @Test
    fun `Given a map of keys to message templates, when formatting with two arguments, it returns success`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "argument 1", "argument 2")

        every { messages["test"] } returns "Template with two args: {0} and {1}."

        messageFormatter.exec(arguments) shouldBe "Template with two args: argument 1 and argument 2."

        verify(exactly = 1) { messages["test"] }
    }

    @Test
    fun `Given a map of keys to message templates, when formatting with two arguments in arbitrary order, it returns success`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "argument 1", "argument 2")

        every { messages["test"] } returns "Template with two args (reversed): {1} and {0}."

        messageFormatter.exec(arguments) shouldBe "Template with two args (reversed): argument 2 and argument 1."

        verify(exactly = 1) { messages["test"] }
    }

    @Test
    fun `Given a map of keys to message templates, when formatting a repeating argument, it returns success`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "argument 1")

        every { messages["test"] } returns "Template with repeating arg: {0} and {0}."

        messageFormatter.exec(arguments) shouldBe "Template with repeating arg: argument 1 and argument 1."

        verify(exactly = 1) { messages["test"] }
    }

    @Test
    fun `Given a map of keys to message templates, when formatting with more arguments than are used in the template, it returns success`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "argument 1", "argument 2")

        every { messages["test"] } returns "Template with only one arg: {0}."

        messageFormatter.exec(arguments) shouldBe "Template with only one arg: argument 1."

        verify(exactly = 1) { messages["test"] }
    }

    @Test
    fun `Given a map of keys to message templates, when formatting ignores an argument, it returns success`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "argument 1", "argument 2")

        every { messages["test"] } returns "Template with only one arg: {1}."

        messageFormatter.exec(arguments) shouldBe "Template with only one arg: argument 2."

        verify(exactly = 1) { messages["test"] }
    }

    @Test
    fun `Given a map of keys to message templates, when template key is null, it returns null`() {
        val arguments: MutableList<Any?> = mutableListOf(null, "argument 1", "argument 2")

        messageFormatter.exec(arguments) shouldBe null
    }

    @Test
    fun `Given a map of keys to message templates, when template message is null, it returns null`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "argument 1", "argument 2")

        every { messages["test"] } returns null

        messageFormatter.exec(arguments) shouldBe null

        verify(exactly = 1) { messages["test"] }
    }

    @Test
    fun `Given a map of keys to message templates, when template argument contains html, it gets escaped`() {
        val arguments: MutableList<Any?> = mutableListOf("test", "<p>html</p>")

        every { messages["test"] } returns "Template with html arg: {0}."

        messageFormatter.exec(arguments) shouldBe "Template with html arg: &lt;p&gt;html&lt;/p&gt;."

        verify(exactly = 1) { messages["test"] }
    }
}
