package eu.tib.orkg.prototype.configuration

import javax.servlet.http.HttpServletRequest
import org.apache.tomcat.util.http.fileupload.FileUploadBase
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.commons.CommonsMultipartResolver

private val allowedMultipartHttpMethods = listOf("POST", "PATCH", "PUT")

@Configuration
class SpringConfiguration {
    @Bean("multipartResolver")
    fun multipartResolver(): CommonsMultipartResolver = object : CommonsMultipartResolver() {
        override fun isMultipart(request: HttpServletRequest): Boolean =
            request.method in allowedMultipartHttpMethods &&
                FileUploadBase.isMultipartContent(ServletRequestContext(request))
    }
}
