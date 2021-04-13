package org.orkg.core.domain

// TODO: might be an inline class
typealias URI = java.net.URI

/** A resource is any entity that can be identified by a URI. */
interface Resource {
  val uri: URI
}

data class Instance(override val uri: URI) : Resource

data class Literal(val value: String)

sealed class Statement {
  data class WithResource(val s: URI, val p: URI, val o: URI) : Statement() {
    constructor(s: String, p: String, o: String) : this(s.toURI(), p.toURI(), o.toURI())
  }
  data class WithLiteral(val s: URI, val p: URI, val o: Literal) : Statement()
}

internal fun String.toURI(): URI = URI(this)
