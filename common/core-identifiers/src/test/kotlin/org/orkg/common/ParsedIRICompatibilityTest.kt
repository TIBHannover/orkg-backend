package org.orkg.common

import io.kotest.matchers.equals.shouldBeEqual
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test

class ParsedIRICompatibilityTest {
    @Test
    fun `two equal ParsedIRIs compare successfully`() {
        EqualsVerifier.forClass(ParsedIRI::class.java)
            // We ignore some warnings because we cannot modify the class.
            // In our case, nullity should not matter much because of Kotlin, and inheritance is not used.
            .withOnlyTheseFields("iri")
            .suppress(Warning.NULL_FIELDS, Warning.STRICT_INHERITANCE)
            .verify()
    }

    @Test
    fun `two equal IRIs compare successfully`() {
        EqualsVerifier.forClass(IRI::class.java).verify()
    }

    @Test
    fun `conversion to string should return the same string`() {
        IRI("some-iri").toString() shouldBeEqual "some-iri"
    }

    @Test
    fun `conversion to string should return the same result RDF implementation`() {
        IRI("some-iri").toString() shouldBeEqual ParsedIRI("some-iri").toString()
    }
}
