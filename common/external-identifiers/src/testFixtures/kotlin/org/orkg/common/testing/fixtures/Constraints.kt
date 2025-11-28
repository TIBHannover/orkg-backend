package org.orkg.common.testing.fixtures

import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import org.orkg.common.VALID_DOI_REGEX
import org.orkg.common.VALID_DOI_URI_REGEX
import org.orkg.common.VALID_GOOGLE_SCHOLAR_ID_REGEX
import org.orkg.common.VALID_GOOGLE_SCHOLAR_ID_URI_REGEX
import org.orkg.common.VALID_HANDLE_REGEX
import org.orkg.common.VALID_HANDLE_URI_REGEX
import org.orkg.common.VALID_ISBN_REGEX
import org.orkg.common.VALID_ISBN_URI_REGEX
import org.orkg.common.VALID_ISSN_REGEX
import org.orkg.common.VALID_ISSN_URI_REGEX
import org.orkg.common.VALID_LINKED_IN_ID_REGEX
import org.orkg.common.VALID_LINKED_IN_ID_URI_REGEX
import org.orkg.common.VALID_OPEN_ALEX_ID_REGEX
import org.orkg.common.VALID_OPEN_ALEX_ID_URI_REGEX
import org.orkg.common.VALID_ORCID_REGEX
import org.orkg.common.VALID_ORCID_URI_REGEX
import org.orkg.common.VALID_RESEARCHER_ID_REGEX
import org.orkg.common.VALID_RESEARCHER_ID_URI_REGEX
import org.orkg.common.VALID_RESEARCH_GATE_ID_REGEX
import org.orkg.common.VALID_RESEARCH_GATE_ID_URI_REGEX
import org.orkg.common.VALID_WIKIDATA_ID_REGEX
import org.orkg.common.VALID_WIKIDATA_ID_URI_REGEX
import org.springframework.restdocs.constraints.Constraint

val doiConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_DOI_REGEX, VALID_DOI_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val googleScholarIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_GOOGLE_SCHOLAR_ID_REGEX, VALID_GOOGLE_SCHOLAR_ID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val handleConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_HANDLE_REGEX, VALID_HANDLE_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val isbnConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_ISBN_REGEX, VALID_ISBN_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val issnConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_ISSN_REGEX, VALID_ISSN_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val linkedInIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_LINKED_IN_ID_REGEX, VALID_LINKED_IN_ID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val openAlexIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_OPEN_ALEX_ID_REGEX, VALID_OPEN_ALEX_ID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val orcidConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_ORCID_REGEX, VALID_ORCID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val researcherIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_RESEARCHER_ID_REGEX, VALID_RESEARCHER_ID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val researchGateIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_RESEARCH_GATE_ID_REGEX, VALID_RESEARCH_GATE_ID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

val wikidataIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to oneOf(VALID_WIKIDATA_ID_REGEX, VALID_WIKIDATA_ID_URI_REGEX),
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

private fun oneOf(vararg patterns: Regex): String =
    patterns.joinToString(separator = "|") { """(?:$it)""" }
