package org.orkg.dataimport.domain.configuration.steps

import org.orkg.contenttypes.output.DoiService
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordParser
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValidatePaperCSVStepConfiguration {
    @Bean
    fun paperCSVRecordParser(
        thingRepository: ThingRepository,
        resourceRepository: ResourceRepository,
        doiService: DoiService,
    ): PaperCSVRecordParser =
        PaperCSVRecordParser(
            thingRepository = thingRepository,
            resourceRepository = resourceRepository,
            doiService = doiService
        )
}
