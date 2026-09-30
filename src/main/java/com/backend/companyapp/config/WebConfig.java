package com.backend.companyapp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // change in production
                .allowedMethods("*");
                // .allowCredentials(true);;
    }

    // @Bean
    // public FilterRegistrationBean<Filter> uploadsSecurityHeaderFilter() {
    //     FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
    //     registrationBean.setFilter(new Filter() {
    //         @Override
    //         public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    //                 throws IOException, ServletException {
    //             if (response instanceof HttpServletResponse httpResponse) {
    //                 httpResponse.setHeader("X-Content-Type-Options", "nosniff");
    //                 httpResponse.setHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox");
    //                 httpResponse.setHeader("X-Frame-Options", "DENY");
    //             }
    //             chain.doFilter(request, response);
    //         }
    //     });
    //     registrationBean.addUrlPatterns("/uploads/*");
    //     return registrationBean;
    // }
}
