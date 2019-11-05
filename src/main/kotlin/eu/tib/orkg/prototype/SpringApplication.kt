package eu.tib.orkg.prototype

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@SpringBootApplication
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder?) =
        builder?.sources(Application::class.java)

    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val config = CorsConfiguration().apply {
            allowCredentials = true
            addAllowedOrigin("http://localhost:3000")
            addAllowedHeader("*")
            addAllowedMethod("*")
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        val bean = FilterRegistrationBean(CorsFilter(source))
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE)
        return bean
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
