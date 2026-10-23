package org.orkg.dataimport.adapter.output.jpa

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.impl.SimpleValueFactory

internal fun parseResource(value: String): Resource =
    if (value.startsWith("_:")) {
        SimpleValueFactory.getInstance().createBNode(value.removePrefix("_:"))
    } else {
        SimpleValueFactory.getInstance().createIRI(value)
    }
