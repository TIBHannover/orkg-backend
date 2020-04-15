package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Neo4j Literal Tests")
internal class Neo4jLiteralTest {

    @Nested
    @DisplayName("Given a Neo4jLiteral with default values")
    inner class GivenANeo4jLiteral {

        private val neo4jLiteral = Neo4jLiteral().apply {
            label = "irrelevant"
        }

        @Test
        @DisplayName("Then the data type is `xsd:string`")
        fun thenTheDataTypeIsString() {
            assertThat(neo4jLiteral.datatype).isEqualTo("xsd:string")
        }

        @Nested
        @DisplayName("When it is converted to a domain object")
        inner class WhenConvertedToDomainObject {

            private val converted = neo4jLiteral.toLiteral()

            @Test
            @DisplayName("Then the data type is `xsd:string`")
            fun thenTheDataTypeIsString() {
                assertThat(converted.datatype).isEqualTo("xsd:string")
            }
        }
    }

    @Nested
    @DisplayName("Given a Neo4jLiteral with non-default values")
    inner class GivenANeo4jLiteralWithNonDefaultValues {

        private val neo4jLiteral = Neo4jLiteral().apply {
            label = "irrelevant"
            datatype = NON_DEFAULT_DATATYPE
        }

        @Test
        @DisplayName("Then the data type is returned")
        fun thenTheDataTypeIsReturned() {
            assertThat(neo4jLiteral.datatype).isEqualTo(NON_DEFAULT_DATATYPE)
        }

        @Nested
        @DisplayName("When it is converted to a domain object")
        inner class WhenConvertedToDomainObject {

            private val converted = neo4jLiteral.toLiteral()

            @Test
            @DisplayName("Then the data type is returned")
            fun thenTheDataTypeIsString() {
                assertThat(converted.datatype).isEqualTo(NON_DEFAULT_DATATYPE)
            }
        }
    }
}

internal const val NON_DEFAULT_DATATYPE = "xs:number"
