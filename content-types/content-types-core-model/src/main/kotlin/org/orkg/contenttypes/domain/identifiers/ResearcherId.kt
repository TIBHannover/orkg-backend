package org.orkg.contenttypes.domain.identifiers

private val VALID_RESEARCHER_ID_REGEX = Regex("""^[A-Z]{1,3}-\d{4}-(?:19|20)\d\d$""")
private val VALID_RESEARCHER_ID_URI_REGEX = Regex("""^https?://www\.researcherid\.com/rid/([A-Z]{1,3}-\d{4}-(?:19|20)\d\d)$""")

@JvmInline
value class ResearcherId private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<ResearcherId>(
        fn = ::ResearcherId,
        validationRegex = VALID_RESEARCHER_ID_REGEX,
        uriValidationRegex = VALID_RESEARCHER_ID_URI_REGEX
    )

    override val uri: String get() = "https://www.researcherid.com/rid/$value"
}
