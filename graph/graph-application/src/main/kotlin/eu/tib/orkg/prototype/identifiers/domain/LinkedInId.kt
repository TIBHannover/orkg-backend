package eu.tib.orkg.prototype.identifiers.domain

private val VALID_LINKED_IN_ID_REGEX = Regex("""^[\p{L}0-9\-&_'’.]+$""")
private val VALID_LINKED_IN_ID_URI_REGEX = Regex("""^https?://\w+\.linkedin\.com/in/([\p{L}0-9\-&_'’.]+)/?""")

@JvmInline
value class LinkedInId private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<LinkedInId>(
        fn = ::LinkedInId,
        validationRegex = VALID_LINKED_IN_ID_REGEX,
        uriValidationRegex = VALID_LINKED_IN_ID_URI_REGEX
    )

    override val uri: String get() = "https://www.linkedin.com/in/$value/"
}
