package org.orkg.contenttypes.adapter.input.rest.jats.markdown

import org.springframework.test.util.XmlExpectationsHelper

internal abstract class AbstractJatsRendererTest {
    private val xml = XmlExpectationsHelper()

    protected infix fun String.shouldBeJatsXml(expected: String) =
        xml.assertXmlEqual(wrapJatsXml(expected), wrapJatsXml(this))

    // Some XML outputs use the xlink prefix in their attributes, which will cause the
    // XML parser used for validation to throw an error when the prefix is not defined
    private fun wrapJatsXml(raw: String): String =
        """
        <wrapper xmlns:xlink="http://www.w3.org/1999/xlink">
            $raw
        </wrapper>
        """.trimIndent()
}
