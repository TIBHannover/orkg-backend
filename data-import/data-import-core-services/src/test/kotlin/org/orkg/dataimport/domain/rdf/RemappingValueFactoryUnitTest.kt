package org.orkg.dataimport.domain.rdf

import io.kotest.matchers.shouldBe
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest

internal class RemappingValueFactoryUnitTest : MockkBaseTest {
    private val mapping = mapOf(
        "https://orkg.org/original" to "https://orkg.org/remapped",
    )

    private val remappingValueFactory = RemappingValueFactory(mapping)

    private val simpleValueFactory = SimpleValueFactory.getInstance()

    @Test
    fun `Given a string that is not contained in the mapping, when creating an iri, it does not remap the iri`() {
        val expected = simpleValueFactory.createIRI("https://orkg.org/example")
        remappingValueFactory.createIRI("https://orkg.org/example") shouldBe expected
    }

    @Test
    fun `Given a string that is contained in the mapping, when creating an iri, it remaps the iri`() {
        val expected = simpleValueFactory.createIRI("https://orkg.org/remapped")
        remappingValueFactory.createIRI("https://orkg.org/original") shouldBe expected
    }

    @Test
    fun `Given a string that is not contained in the mapping, when creating an iri with namespace and local name, it does not remap the iri`() {
        val expected = simpleValueFactory.createIRI("https://orkg.org/example")
        remappingValueFactory.createIRI("https://orkg.org/", "example") shouldBe expected
    }

    @Test
    fun `Given a string that is contained in the mapping, when creating an iri with namespace and local name, it remaps the iri`() {
        val expected = simpleValueFactory.createIRI("https://orkg.org/remapped")
        remappingValueFactory.createIRI("https://orkg.org/", "original") shouldBe expected
    }

    @Test
    fun `Given a subject, predicate and object that are not contained in the mapping, when creating a statement, it does not remap the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/subject")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/predicate")
        val `object` = simpleValueFactory.createLiteral("literal")
        val expected = simpleValueFactory.createStatement(subject, predicate, `object`)

        remappingValueFactory.createStatement(subject, predicate, `object`) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate and object that are contained in the mapping, when creating a statement, it remaps the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/original")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/original")
        val `object` = simpleValueFactory.createIRI("https://orkg.org/original")
        val expected = simpleValueFactory.createStatement(
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
        )

        remappingValueFactory.createStatement(subject, predicate, `object`) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate and object that are not contained in the mapping, when creating a triple, it does not remap the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/subject")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/predicate")
        val `object` = simpleValueFactory.createLiteral("literal")
        val expected = simpleValueFactory.createTriple(subject, predicate, `object`)

        remappingValueFactory.createTriple(subject, predicate, `object`) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate and object that are contained in the mapping, when creating a triple, it remaps the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/original")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/original")
        val `object` = simpleValueFactory.createIRI("https://orkg.org/original")
        val expected = simpleValueFactory.createTriple(
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
        )

        remappingValueFactory.createTriple(subject, predicate, `object`) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate, object and context that are not contained in the mapping, when creating a statement, it does not remap the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/subject")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/predicate")
        val `object` = simpleValueFactory.createLiteral("literal")
        val context = simpleValueFactory.createTriple(subject, predicate, `object`)
        val expected = simpleValueFactory.createStatement(subject, predicate, `object`, context)

        remappingValueFactory.createStatement(subject, predicate, `object`, context) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate, object and context that are contained in the mapping, when creating a statement, it remaps the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/original")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/original")
        val `object` = simpleValueFactory.createIRI("https://orkg.org/original")
        val context = simpleValueFactory.createTriple(subject, predicate, `object`)
        val expected = simpleValueFactory.createStatement(
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createTriple(
                simpleValueFactory.createIRI("https://orkg.org/remapped"),
                simpleValueFactory.createIRI("https://orkg.org/remapped"),
                simpleValueFactory.createIRI("https://orkg.org/remapped"),
            ),
        )

        remappingValueFactory.createStatement(subject, predicate, `object`, context) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate, object and context (null) that are not contained in the mapping, when creating a statement, it does not remap the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/subject")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/predicate")
        val `object` = simpleValueFactory.createLiteral("literal")
        val context = null
        val expected = simpleValueFactory.createStatement(subject, predicate, `object`, context)

        remappingValueFactory.createStatement(subject, predicate, `object`, context) shouldBe expected
    }

    @Test
    fun `Given a subject, predicate, object and context (null) that are contained in the mapping, when creating a statement, it remaps the entities`() {
        val subject = simpleValueFactory.createIRI("https://orkg.org/original")
        val predicate = simpleValueFactory.createIRI("https://orkg.org/original")
        val `object` = simpleValueFactory.createIRI("https://orkg.org/original")
        val context = null
        val expected = simpleValueFactory.createStatement(
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            simpleValueFactory.createIRI("https://orkg.org/remapped"),
            context,
        )

        remappingValueFactory.createStatement(subject, predicate, `object`, context) shouldBe expected
    }
}
