package org.orkg.dataimport.domain.configuration

import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.orkg.dataimport.domain.rdf.RemappingValueFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValueFactoryConfiguration {
    @Bean
    fun valueFactory(): ValueFactory =
        RemappingValueFactory(
            mapOf(
                // Classes
                "https://doi.org/21.T11969/0424f6e7026fa4bc2c4a" to "http://www.w3.org/ns/csvw#Table",
                "https://doi.org/21.T11969/4607bc7c42ac8db29bfc" to "http://www.w3.org/ns/csvw#Cell",
                "https://doi.org/21.T11969/65ba00e95e60fb8971e6" to "http://www.w3.org/ns/csvw#Column",
                "https://doi.org/21.T11969/9bf7a8e8909bfd491b38" to "http://www.w3.org/ns/csvw#Row",
                // Predicates
                "https://doi.org/21.T11969/0424f6e7026fa4bc2c4a#label" to RDFS.LABEL.toString(),
                "https://doi.org/21.T11969/0424f6e7026fa4bc2c4a#columns" to "http://orkg.org/orkg/predicate/CSVW_Columns",
                "https://doi.org/21.T11969/0424f6e7026fa4bc2c4a#rows" to "http://orkg.org/orkg/predicate/CSVW_Rows",
                "https://doi.org/21.T11969/65ba00e95e60fb8971e6#number" to "http://orkg.org/orkg/predicate/CSVW_Number",
                "https://doi.org/21.T11969/65ba00e95e60fb8971e6#titles" to "http://orkg.org/orkg/predicate/CSVW_Titles",
                "https://doi.org/21.T11969/9bf7a8e8909bfd491b38#number" to "http://orkg.org/orkg/predicate/CSVW_Number",
                "https://doi.org/21.T11969/9bf7a8e8909bfd491b38#titles" to "http://orkg.org/orkg/predicate/CSVW_Titles",
                "https://doi.org/21.T11969/9bf7a8e8909bfd491b38#cells" to "http://orkg.org/orkg/predicate/CSVW_Cells",
                "https://doi.org/21.T11969/4607bc7c42ac8db29bfc#value" to "http://orkg.org/orkg/predicate/CSVW_Value",
                "https://doi.org/21.T11969/4607bc7c42ac8db29bfc#column" to "http://orkg.org/orkg/predicate/CSVW_Column",
            ),
        )
}
