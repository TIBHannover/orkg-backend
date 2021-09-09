package eu.tib.orkg.prototype.statements.cypher

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@DisplayName("Cypher Extensions")
class CypherExtensionTest {
    @Nested
    @DisplayName("When converting a class to Cypher")
    inner class ClassToCypher {
        @Test
        @DisplayName("all values are passed when provided")
        fun allProvided() {
            // We use randomly generated strings here, because the concrete values do not matter.
            val actual = Class(
                id = ClassId("kuk6exeaLaeb"),
                label = "Eish7ohShoh0",
                uri = URI.create("fu0coobaeroY"),
                createdAt = OffsetDateTime.parse("2021-09-07T16:26:30.099614+02:00"),
                createdBy = ContributorId(UUID.fromString("122f8056-52ea-486c-8243-192ca9deddb8")),
            ).asCypherString("n")
            assertThat(actual).isEqualTo(
                """
                |(n:`Class`:`Thing` {
                |class_id:"kuk6exeaLaeb",
                |label:"Eish7ohShoh0",
                |uri:"fu0coobaeroY",
                |created_at:"2021-09-07T16:26:30.099614+02:00",
                |created_by:"122f8056-52ea-486c-8243-192ca9deddb8"}
                |)""".trimMargin().replace("\n", "")
            )
        }
    }

    // TODO: uri is null
}
