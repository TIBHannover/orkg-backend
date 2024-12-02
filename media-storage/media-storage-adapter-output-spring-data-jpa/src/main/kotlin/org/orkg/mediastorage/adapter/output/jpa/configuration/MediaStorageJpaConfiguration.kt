package org.orkg.mediastorage.adapter.output.jpa.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories("org.orkg.mediastorage.adapter.output.jpa.internal")
@EntityScan("org.orkg.mediastorage.adapter.output.jpa.internal")
@ComponentScan(basePackages = ["org.orkg.mediastorage.adapter.output.jpa"])
class MediaStorageJpaConfiguration
