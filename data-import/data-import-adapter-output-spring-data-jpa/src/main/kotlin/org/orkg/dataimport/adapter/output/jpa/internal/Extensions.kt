package org.orkg.dataimport.adapter.output.jpa.internal

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.helpers.NTriplesUtil

internal fun parseResource(value: String, type: Value.Type): Resource =
    when (type) {
        Value.Type.IRI -> SimpleValueFactory.getInstance().createIRI(value)
        Value.Type.BNode -> SimpleValueFactory.getInstance().createBNode(value.removePrefix("_:"))
        Value.Type.Literal -> throw IllegalArgumentException("Invalid value type: $type")
        Value.Type.Triple -> throw IllegalArgumentException("Unsupported value type: $type")
    }

internal fun parseValue(value: String, type: Value.Type): Value =
    when (type) {
        Value.Type.IRI -> SimpleValueFactory.getInstance().createIRI(value)
        Value.Type.BNode -> SimpleValueFactory.getInstance().createBNode(value.removePrefix("_:"))
        Value.Type.Literal -> NTriplesUtil.parseLiteral(value, SimpleValueFactory.getInstance())
        Value.Type.Triple -> throw IllegalArgumentException("Unsupported value type: $type")
    }
