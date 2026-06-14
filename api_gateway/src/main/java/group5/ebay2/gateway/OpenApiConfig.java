package group5.ebay2.gateway;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("eBay Clone API")
                        .description("API Gateway — single entry point for all services. JWT required on all routes except /auth/**")
                        .version("1.0.0"));
    }
}
