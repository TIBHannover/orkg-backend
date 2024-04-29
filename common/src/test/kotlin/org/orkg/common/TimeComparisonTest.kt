package org.orkg.common

import java.time.OffsetDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * This class is a demonstrator / reminder on comparing date and time classes.
 *
 * The example values were taken from a failing pipeline test.
 * Postgres only stores date and time values (even with offset) in UTC, so a query will always return UTC values.
 * The values are correct, but connect be compared directly to the original value using equals().
 */
class TimeComparisonTest {
    private val withOffset = OffsetDateTime.parse("2023-11-30T09:25:14.049+01:00")
    private val withZulu = OffsetDateTime.parse("2023-11-30T08:25:14.049Z")

    @Test
    fun shouldNotBeEqualWhenComparedWithEquals() {
        assertThat(withOffset == withZulu).isFalse()
    }

    @Test
    fun shouldBeEqualWhenComparedWithIsEqual() {
        assertThat(withOffset.isEqual(withZulu)).isTrue()
    }
}
