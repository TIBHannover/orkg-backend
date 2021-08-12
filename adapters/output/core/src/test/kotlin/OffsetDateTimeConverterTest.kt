package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.neo4j.driver.Values
import java.time.OffsetDateTime

class OffsetDateTimeConverterTest {
    @Test
    fun `Converting back and forth via OffsetDateTime gives the same result`() {
        val converter = OffsetDateTimeConverter()
        val expected = "2019-12-16T16:54:05.404+01:00"

        val actual = converter.write(converter.read(Values.value(expected))).asString()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Converting back and forth via String gives the same result`() {
        val converter = OffsetDateTimeConverter()
        val expected = OffsetDateTime.parse("2019-12-16T16:54:05.404+01:00")

        val actual = converter.read(converter.write(expected))

        assertThat(actual).isEqualTo(expected)
    }
}
