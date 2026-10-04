package com.idlewheels.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionAuthInterceptor sessionAuthInterceptor;
    private final CsrfInterceptor csrfInterceptor;
    private final String storageDirectory;

    public WebConfig(
            SessionAuthInterceptor sessionAuthInterceptor,
            CsrfInterceptor csrfInterceptor,
            @Value("${idlewheels.storage.local-dir:uploads}") String storageDirectory
    ) {
        this.sessionAuthInterceptor = sessionAuthInterceptor;
        this.csrfInterceptor = csrfInterceptor;
        this.storageDirectory = storageDirectory;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/owner/**", "/messages", "/messages/**", "/profile");
        registry.addInterceptor(csrfInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(storageDirectory).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
