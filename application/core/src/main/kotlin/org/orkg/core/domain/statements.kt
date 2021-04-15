package org.orkg.core.domain

annotation class RDFLike

typealias Namespace = String

@RDFLike
sealed class IRI {
  data class Absolute(private val iri: String) : IRI()
  data class Namespaced(private val namespace: Namespace, private val part: String) : IRI()
}

@RDFLike
interface Resource { // FIXME: resource in RDF-world
  val iri: IRI
}

data class Instance(override val iri: IRI) : Resource

data class Predicate(override val iri: IRI) : Resource

data class Class(override val iri: IRI) : Resource

// TODO: Triple
@RDFLike sealed class Triple

// TODO: something with an ID
sealed class Statement {
  data class WithResource(val s: IRI, val p: IRI, val o: IRI) : Statement() {
    // constructor(s: String, p: String, o: String) : this(s.toIRI(), p.toIRI(), o.toIRI())
  }
  data class WithLiteral(val s: IRI, val p: IRI, val o: Literal<Any>) : Statement()
}

fun String.toIRI(): IRI = IRI.Absolute(this)

//// EXAMPLES

fun foo(l: Literal<Any>): Int {
  return when (l) {
    is StringLiteral -> l.value.toInt() // fall flat on your face...
    is IntLiteral -> l.value
    // is Literal.GenericLiteral<*> -> throw IllegalStateException("I really cannot deal with that
    // right now!")
    is GenericLiteral<*> -> l.value.toString().toInt()
  }
}

fun bar(l: StringLiteral): String = l.value
