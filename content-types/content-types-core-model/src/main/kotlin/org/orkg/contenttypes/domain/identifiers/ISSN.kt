package org.orkg.contenttypes.domain.identifiers

private val VALID_ISSN_REGEX = Regex("""^(\d{4}-\d{3}(?:\d|X))$""")
private val VALID_ISSN_URI_REGEX = Regex("""^https?://(?:portal\.)?issn\.org/resource/(?:issn|ISSN)/(\d{4}-\d{3}(?:\d|X))$""")

@JvmInline
value class ISSN private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<ISSN>(
        fn = ::ISSN,
        validationRegex = VALID_ISSN_REGEX,
        uriValidationRegex = VALID_ISSN_URI_REGEX
    )

    override val uri: String get() = "https://portal.issn.org/resource/ISSN/$value"
}
