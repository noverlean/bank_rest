package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Card Management System API")
                        .version("1.0.0")
                        .description("""
                    # REST API для управления банковскими картами
                    
                    ## Основные возможности:
                    - 🔐 **Аутентификация** (JWT токены)
                    - 👥 **Управление пользователями** (ADMIN/USER роли)
                    - 💳 **Управление банковскими картами**
                    - 💸 **Переводы между картами**
                    
                    ## Роли:
                    - **ADMIN** - Полный доступ ко всем операциям
                    - **USER** - Доступ только к своим картам и операциям
                    
                    ## Авторизация:
                    Все запросы (кроме аутентификации) требуют JWT токена.
                    Добавьте заголовок: `Authorization: Bearer {your-jwt-token}`
                    """)
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@bank.com")
                                .url("https://bank.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Локальный сервер разработки"),
                        new Server()
                                .url("https://api.bank.com")
                                .description("Продакшен сервер")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT токен аутентификации")));
    }
}