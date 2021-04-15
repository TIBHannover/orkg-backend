package org.orkg.core.domain

/** A representation of a value of a specific datatype. */
@RDFLike
sealed class Literal<out T : Any> {
  /** The value itself. */
  abstract val value: T

  /** The datatype of the value. */
  abstract val datatype: String // FIXME: URI/iri
}

/** A generic literal of a specified datatype. */
data class GenericLiteral<out T : Any>(override val value: Any, override val datatype: String) :
    Literal<Any>()

/** A string value. */
data class StringLiteral(override val value: String) : Literal<String>() {
  override val datatype: String
    get() = "xsd:string"
}

/** A integer value. */
data class IntLiteral(override val value: Int) : Literal<Int>() {
  override val datatype: String
    get() = "xsd:number"
}
