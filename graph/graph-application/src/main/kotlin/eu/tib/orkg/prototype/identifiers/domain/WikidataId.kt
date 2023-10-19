package eu.tib.orkg.prototype.identifiers.domain

private val VALID_WIKIDATA_ID_REGEX = Regex("""^Q\d+$""")
private val VALID_WIKIDATA_ID_URI_REGEX = Regex("""^https?://(?:www\.)?wikidata\.org/wiki/(Q\d+)$""")

@JvmInline
value class WikidataId private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<WikidataId>(
        fn = ::WikidataId,
        validationRegex = VALID_WIKIDATA_ID_REGEX,
        uriValidationRegex = VALID_WIKIDATA_ID_URI_REGEX
    )

    override val uri: String get() = "https://www.wikidata.org/wiki/$value"
}
