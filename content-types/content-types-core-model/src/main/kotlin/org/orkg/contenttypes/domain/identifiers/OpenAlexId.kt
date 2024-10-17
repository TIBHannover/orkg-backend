package org.orkg.contenttypes.domain.identifiers

private val VALID_OPEN_ALEX_ID_REGEX = Regex("""^([ACIWTSPFaciwtspf][1-9]\d{3,9})$""")
private val VALID_OPEN_ALEX_ID_URI_REGEX = Regex("""^https?://(?:explore\.)?openalex\.org/(?:(?:authors|concepts|institutions|venues|works|sources|publishers|funders|topics)/)?([ACIWTSPFaciwtspf][1-9]\d{3,9})$""")

@JvmInline
value class OpenAlexId private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<OpenAlexId>(
        fn = ::OpenAlexId,
        validationRegex = VALID_OPEN_ALEX_ID_REGEX,
        uriValidationRegex = VALID_OPEN_ALEX_ID_URI_REGEX
    )

    override val uri: String get() = "https://openalex.org/$value"
}
