package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.common.testing.fixtures.Assets.modelJson
import org.orkg.common.testing.fixtures.Assets.representationJson
import org.orkg.graph.adapter.input.rest.configuration.GraphSpringConfig
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.Jackson2Tester
import org.springframework.test.context.ContextConfiguration
import java.time.OffsetDateTime

@JsonTest
@ContextConfiguration(classes = [CommonSpringConfig::class, GraphSpringConfig::class])
internal class StatementSerializationJsonTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var json: Jackson2Tester<GeneralStatement>

    /**
     * Statement Representation V1
     *
     * Missing fields:
     * - modifiable
     * - index
     */
    @Test
    fun `Given a statement representation v1 serialization, it correctly parses the statement`() {
        val statementRepresentationV1 = representationJson("statements/v1")

        objectMapper.readValue(statementRepresentationV1, GeneralStatement::class.java).asClue {
            it.id shouldBe StatementId("S353060")
            it.subject.shouldBeInstanceOf<Resource>()
            it.predicate.shouldBeInstanceOf<Predicate>()
            it.`object`.shouldBeInstanceOf<Literal>()
            it.createdAt shouldBe OffsetDateTime.parse("2021-05-13T10:21:13.081955+02:00")
            it.createdBy shouldBe ContributorId("64acea2a-d2d7-4b28-9ada-8f9c0c5afea2")
            it.modifiable shouldBe true
            it.index shouldBe null
        }
    }

    /**
     * Statement Representation V2
     *
     * Missing fields:
     * - modifiable
     */
    @Test
    fun `Given a statement representation v2 serialization, it correctly parses the statement`() {
        val statementRepresentationV2 = representationJson("statements/v2")

        objectMapper.readValue(statementRepresentationV2, GeneralStatement::class.java).asClue {
            it.id shouldBe StatementId("S353060")
            it.subject.shouldBeInstanceOf<Resource>()
            it.predicate.shouldBeInstanceOf<Predicate>()
            it.`object`.shouldBeInstanceOf<Literal>()
            it.createdAt shouldBe OffsetDateTime.parse("2021-05-13T10:21:13.081955+02:00")
            it.createdBy shouldBe ContributorId("64acea2a-d2d7-4b28-9ada-8f9c0c5afea2")
            it.modifiable shouldBe true
            it.index shouldBe 1
        }
    }

    /**
     * Statement Representation V3
     */
    @Test
    fun `Given a statement representation v3 serialization, it correctly parses the statement`() {
        val statementRepresentationV3 = representationJson("statements/v3")

        objectMapper.readValue(statementRepresentationV3, GeneralStatement::class.java).asClue {
            it.id shouldBe StatementId("S353060")
            it.subject.shouldBeInstanceOf<Resource>()
            it.predicate.shouldBeInstanceOf<Predicate>()
            it.`object`.shouldBeInstanceOf<Literal>()
            it.createdAt shouldBe OffsetDateTime.parse("2021-05-13T10:21:13.081955+02:00")
            it.createdBy shouldBe ContributorId("64acea2a-d2d7-4b28-9ada-8f9c0c5afea2")
            it.modifiable shouldBe true
            it.index shouldBe 1
        }
    }

    @Test
    fun `Given a statement domain model serialization, it correctly parses the statement`() {
        val statement = modelJson("orkg/statement")

        objectMapper.readValue(statement, GeneralStatement::class.java).asClue {
            it.id shouldBe StatementId("S353060")
            it.subject.shouldBeInstanceOf<Resource>()
            it.predicate.shouldBeInstanceOf<Predicate>()
            it.`object`.shouldBeInstanceOf<Literal>()
            it.createdAt shouldBe OffsetDateTime.parse("2019-12-19T15:07:02.204+01:00")
            it.createdBy shouldBe ContributorId("64acea2a-d2d7-4b28-9ada-8f9c0c5afea2")
            it.modifiable shouldBe true
            it.index shouldBe 5
        }
    }

    @Test
    fun `Given a statement, it gets correctly serialized to json`() {
        val statement = createStatement(
            id = StatementId("S353060"),
            subject = createResource(),
            predicate = createPredicate(),
            `object` = createLiteral(),
            createdAt = OffsetDateTime.parse("2019-12-19T15:07:02.204+01:00"),
            createdBy = ContributorId("64acea2a-d2d7-4b28-9ada-8f9c0c5afea2"),
            modifiable = true,
            index = 5
        )

        val expected = modelJson("orkg/statement")

        assertThat(json.write(statement)).isEqualToJson(expected.byteInputStream())
    }
}
