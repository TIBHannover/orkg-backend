package org.orkg.common.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient

@Configuration
class HttpClientConfiguration {
    @Bean
    fun httpClient(): HttpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // JDK 21 does not handle HTTP/2 GOAWAY frames correctly. See https://bugs.openjdk.org/browse/JDK-8335181
            .build()
}
