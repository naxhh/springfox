package springfox.documentation.spring.web.readers.parameter

import com.fasterxml.classmate.ResolvedType
import com.fasterxml.classmate.TypeResolver
import com.wordnik.swagger.annotations.ApiParam
import org.springframework.core.MethodParameter
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.service.ResolvedMethodParameter
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.GenericTypeNamingStrategy
import springfox.documentation.spi.service.contexts.ParameterContext
import springfox.documentation.spring.web.dummy.DummyClass
import springfox.documentation.spring.web.mixins.ModelProviderForServiceSupport
import springfox.documentation.spring.web.mixins.RequestMappingSupport
import springfox.documentation.spring.web.plugins.DocumentationContextSpec

@Mixin([RequestMappingSupport, ModelProviderForServiceSupport])
class ParameterMultiplesReaderSpec extends DocumentationContextSpec {

  def sut = new ParameterMultiplesReader();
  
  def "Should support all documentation types"() {
    expect:
      sut.supports(DocumentationType.SPRING_WEB)
      sut.supports(DocumentationType.SWAGGER_12)
      sut.supports(DocumentationType.SWAGGER_2)
  }

  def "param multiples for default reader"() {
    given:
      MethodParameter methodParameter = Stub(MethodParameter)
      methodParameter.getParameterAnnotation(ApiParam.class) >> apiParamAnnotation
      methodParameter.getParameterType() >> paramType
      ResolvedType resolvedType = paramType != null ? new TypeResolver().resolve(paramType) : null
      ResolvedMethodParameter resolvedMethodParameter = new ResolvedMethodParameter(methodParameter, resolvedType)
      ParameterContext parameterContext = new ParameterContext(resolvedMethodParameter, new ParameterBuilder(),
              context(), Mock(GenericTypeNamingStrategy))

    when:
      sut.apply(parameterContext)
    then:
      parameterContext.parameterBuilder().build().isAllowMultiple() == expected
    where:
      apiParamAnnotation                        | paramType                       | expected
      [allowMultiple: { -> true }] as ApiParam  | null                            | false
      [allowMultiple: { -> false }] as ApiParam | String[].class                  | true
      [allowMultiple: { -> false }] as ApiParam | DummyClass.BusinessType[].class | true
      null                                      | String[].class                  | true
      null                                      | List.class                      | true
      null                                      | Collection.class                | true
      null                                      | Set.class                       | true
      null                                      | Vector.class                    | true
      null                                      | Object[].class                  | true
      null                                      | Integer.class                   | false
      null                                      | Iterable.class                  | true
  }


}