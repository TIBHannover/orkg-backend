package eu.tib.orkg.prototype.identifiers.domain

private val VALID_ORCID_REGEX = Regex("""^\d{4}-\d{4}-\d{4}-\d{3}[\dX]$""")
private val VALID_ORCID_URI_REGEX = Regex("""^https?://orcid\.org/(\d{4}-\d{4}-\d{4}-\d{3}[\dX])$""")

@JvmInline
value class ORCID private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<ORCID>(
        fn = ::ORCID,
        validationRegex = VALID_ORCID_REGEX,
        uriValidationRegex = VALID_ORCID_URI_REGEX
    )

    override val uri: String get() = "https://orcid.org/$value"
}
