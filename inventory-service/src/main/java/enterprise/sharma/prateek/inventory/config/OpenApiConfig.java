package enterprise.sharma.prateek.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI inventoryServiceOpenAPI() {

		return new OpenAPI()
				.info(new Info().title("Inventory Service API").description("Enterprise Inventory Management API")
						.version("1.0.0").contact(new Contact().name("Enterprise Platform Team")));
	}

}
