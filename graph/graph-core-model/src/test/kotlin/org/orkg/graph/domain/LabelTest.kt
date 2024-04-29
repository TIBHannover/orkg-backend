package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class LabelTest {
    @Test
    fun `A string with no newlines can be converted to a label`() {
        val actual = Label.ofOrNull("aa1765521c66bf7b1497cb384d6dc4b1")
        assertThat(actual).isNotNull
    }

    @ParameterizedTest
    @ValueSource(strings = ["\nabcd", "ab\ncd", "abcd\n"])
    fun `A string containing a newline cannot be converted to a label`(input: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Label.of(input)
        }
    }

    @Test
    fun `An empty string is a valid label`() {
        assertThatCode { Label.of("") }.doesNotThrowAnyException()
    }

    @ParameterizedTest
    @ValueSource(strings = ["  ", "\t  ", " \t ", "  \t", "  \t \n "])
    fun `A blank string cannot be converted to a label`(input: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Label.of(input)
        }
    }

    @Test
    fun `A valid label can be printed, because it is not sensitive`() {
        assertThat(Label.of("We were here!").value).isEqualTo("We were here!")
    }

    @Test
    fun `A string exceeding the maximum label length cannot be converted to a label`() {
        assertThrows(IllegalArgumentException::class.java) {
            Label.of("a".repeat(MAX_LABEL_LENGTH + 1))
        }
    }
}
