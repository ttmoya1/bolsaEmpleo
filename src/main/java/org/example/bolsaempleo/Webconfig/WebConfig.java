package org.example.bolsaempleo.Webconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String rutaAbsoluta = "file:" + Paths.get("uploads/curricula/")
                .toAbsolutePath()
                .toString()
                .replace("\\", "/")
                + "/";

        registry.addResourceHandler("/curricula/**")
                .addResourceLocations(rutaAbsoluta);
    }
}