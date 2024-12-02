package org.orkg.common.configuration

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.MediaTypeCapabilityRegistry
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.DIRECT
import org.springframework.http.MediaType
import org.springframework.util.MimeTypeUtils
import org.springframework.web.accept.ContentNegotiationManager
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = DIRECT)
class PagedSerializationConfiguration

@Configuration
@Import(MediaTypeCapabilityRegistry::class)
class WebMvcConfiguration(
    private val mediaTypeCapabilityRegistry: MediaTypeCapabilityRegistry,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(MediaTypeCapabilitiesResolver(mediaTypeCapabilityRegistry))
    }
}

class MediaTypeCapabilitiesResolver(
    private val mediaTypeCapabilityRegistry: MediaTypeCapabilityRegistry,
    private val contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(methodParameter: MethodParameter): Boolean =
        MediaTypeCapabilities::class.java == methodParameter.parameterType

    override fun resolveArgument(
        methodParameter: MethodParameter,
        modelAndViewContainer: ModelAndViewContainer?,
        nativeWebRequest: NativeWebRequest,
        webDataBinderFactory: WebDataBinderFactory?
    ): MediaTypeCapabilities {
        // find producible declarations analogue to RequestMappingHandlerMapping.createRequestMappingInfo
        val producibleTypes =
            AnnotatedElementUtils.findMergedAnnotation(methodParameter.annotatedElement, RequestMapping::class.java)!!.produces
                .ifEmpty {
                    // use producible declarations from class when method contains no producible declarations
                    // analogue to RequestMappingHandlerMapping.getMappingForMethod
                    AnnotatedElementUtils.findMergedAnnotation(methodParameter.containingClass, RequestMapping::class.java)?.produces
                }
                .orEmpty()
                .map(MediaType::parseMediaType)
        // find response content-type analogue to AbstractMessageConverterMethodProcessor.writeWithMessageConverters
        val acceptableTypes = contentNegotiationManager.resolveMediaTypes(nativeWebRequest)
        val mediaTypesToUse = mutableListOf<MediaType>()
        acceptableTypes.forEach { requestedType ->
            producibleTypes.forEach { producibleType ->
                if (requestedType.isCompatibleWith(producibleType)) {
                    mediaTypesToUse.add(getMostSpecificMediaType(requestedType, producibleType))
                }
            }
        }
        MimeTypeUtils.sortBySpecificity(mediaTypesToUse)
        return mediaTypesToUse.firstOrNull { it.isConcrete }
            ?.let { MediaTypeCapabilities.parse(it, mediaTypeCapabilityRegistry.getSupportedCapabilities(it)) }
            ?: MediaTypeCapabilities.EMPTY
    }

    private fun getMostSpecificMediaType(acceptType: MediaType, produceType: MediaType): MediaType {
        val produceTypeToUse = produceType.copyQualityValue(acceptType)
        return if (acceptType.isLessSpecific(produceTypeToUse)) produceTypeToUse else acceptType
    }
}
