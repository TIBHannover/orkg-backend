package eu.tib.orkg.prototype.statements.cypher

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Resource


internal fun Class.asCypherString(named: String): String {
    val uri = this.uri?.toString()?.quote()
    return """($named:`Class`:`Thing` {class_id:"${this.id}",label:"${this.label}",uri:$uri,created_at:"${this.createdAt}",created_by:"${this.createdBy}"})"""
}

internal fun Resource.asCypherString(named: String): String {
    val classes = this.classes.joinToString(":", ":") { "`$it`" }
    return """($named:Thing:Resource$classes {resource_id: "R1", label: "some resource",created_at:"${this.createdAt}",created_by:"${this.createdBy}",observatory_id:"${this.observatoryId}",organization_id:"${this.organizationId}",extraction_method:"${this.extractionMethod}"})"""
}

internal fun String.quote(quote: CharSequence = "\""): String = "$quote$this$quote"
