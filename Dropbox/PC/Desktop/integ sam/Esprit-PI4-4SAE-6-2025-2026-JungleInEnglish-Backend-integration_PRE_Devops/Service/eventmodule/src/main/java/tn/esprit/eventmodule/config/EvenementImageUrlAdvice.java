package tn.esprit.eventmodule.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tn.esprit.eventmodule.entity.Evenement;

import java.util.List;

/**
 * Rewrites imageUrl in Evenement responses to absolute URLs so the frontend
 * can use them directly in &lt;img src&gt; (avoids broken images when API is on a different origin).
 */
@RestControllerAdvice(basePackages = "tn.esprit.eventmodule.controller")
public class EvenementImageUrlAdvice implements ResponseBodyAdvice<Object> {

    @Value("${app.public-base-url:http://localhost:8082}")
    private String baseUrl;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        return Evenement.class.equals(type) || (List.class.isAssignableFrom(type));
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) return body;
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        if (body instanceof Evenement e) {
            rewriteImageUrl(e, base);
            return body;
        }
        if (body instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Evenement) {
            for (Object item : list) {
                rewriteImageUrl((Evenement) item, base);
            }
        }
        return body;
    }

    private void rewriteImageUrl(Evenement e, String base) {
        String url = e.getImageUrl();
        if (url != null && url.startsWith("/") && !url.startsWith("http")) {
            e.setImageUrl(base + url.substring(1));
        }
    }
}
