package org.orkg.contenttypes.adapter.input.rest.mapping

import org.w3c.dom.Document
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Document.toXml(prettyPrint: Boolean = false): String =
    StringWriter().apply {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")

        if (prettyPrint) {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        }

        transformer.transform(DOMSource(documentElement), StreamResult(this))
    }.toString()
