package org.orkg.common

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RealNumberUnitTest {
    @ParameterizedTest
    @ValueSource(strings = [
        "1", "0.1", "00.1", ".1", "1.1", "1.", "1.0", "1.00",
        "12", "0.12", "00.12", ".12", "12.12", "12.", "12.0", "12.00",
        "+1", "+0.1", "+00.1", "+.1", "+1.1", "+1.", "+1.0", "+1.00",
        "+12", "+0.12", "+00.12", "+.12", "+12.12", "+12.", "+12.0", "+12.00",
        "-1", "-0.1", "-00.1", "-.1", "-1.1", "-1.", "-1.0", "-1.00",
        "-12", "-0.12", "-00.12", "-.12", "-1.12", "-12.", "-12.0", "-12.00",
    ])
    fun `Given a number, when instantiated, it returns success`(value: String) {
        shouldNotThrow<IllegalArgumentException> { RealNumber(value) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "-", "-.", "+", "+.", "+-", "a", "abc", "+a", "+-1", "1.1E-10", "+ 1", "- 1", " +1", " 1", " -1", "+1 ", "1 ", "-1 "])
    fun `Given a number, when instantiated, it throws an exception`(value: String) {
        shouldThrow<IllegalArgumentException> { RealNumber(value) }.asClue {
            it.message shouldBe """"$value" is not a valid number."""
        }
    }
}
