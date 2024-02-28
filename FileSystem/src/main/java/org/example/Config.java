package org.example;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@org.springframework.context.annotation.Configuration
public class Config implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dirName = "videos";
        String path = System.getProperty("user.dir") + "/" + dirName + "/";
        registry.addResourceHandler("/videos/**").addResourceLocations("file:" + path);
    }

}
