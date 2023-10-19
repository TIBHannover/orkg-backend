package eu.tib.orkg.prototype.identifiers.domain

private val VALID_GOOGLE_SCHOLAR_ID_REGEX = Regex("""^[-_0-9A-Za-z]{12}$""")
private val VALID_GOOGLE_SCHOLAR_ID_URI_REGEX = Regex("""^https?://scholar\.google\.[a-z.]+/citations\?user=([-_0-9A-Za-z]{12})""")

@JvmInline
value class GoogleScholarId private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<GoogleScholarId>(
        fn = ::GoogleScholarId,
        validationRegex = VALID_GOOGLE_SCHOLAR_ID_REGEX,
        uriValidationRegex = VALID_GOOGLE_SCHOLAR_ID_URI_REGEX
    )

    override val uri: String get() = "https://scholar.google.com/citations?user=$value"
}
