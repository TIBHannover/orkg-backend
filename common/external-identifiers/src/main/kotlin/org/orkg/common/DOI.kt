package org.orkg.common

private val VALID_DOI_REGEX = Regex("""^10(?:\.[1-9]\d*)+/\S+$""")
private val VALID_DOI_URI_REGEX = Regex("""^https?://(?:dx\.|www\.)?doi\.org/(10(?:\.[1-9]\d*)+/\S+)$""")

@JvmInline
value class DOI private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<DOI>(
        fn = ::DOI,
        validationRegex = VALID_DOI_REGEX,
        uriValidationRegex = VALID_DOI_URI_REGEX,
    )

    override val uri: String get() = "https://doi.org/$value"
}
