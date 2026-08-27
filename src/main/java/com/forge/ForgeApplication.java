package com.forge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForgeApplication.class, args);
        System.out.println("""
                \s
                ███████╗██╗████████╗███╗   ███╗██╗███╗   ██╗██████╗
                ██╔════╝██║╚══██╔══╝████╗ ████║██║████╗  ██║██╔══██╗
                █████╗  ██║   ██║   ██╔████╔██║██║██╔██╗ ██║██║  ██║
                ██╔══╝  ██║   ██║   ██║╚██╔╝██║██║██║╚██╗██║██║  ██║
                ██║     ██║   ██║   ██║ ╚═╝ ██║██║██║ ╚████║██████╔╝
                ╚═╝     ╚═╝   ╚═╝   ╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═════╝
                \s
                AI-Powered Personal Fitness Analytics Platform
                Swagger UI: http://localhost:8080/swagger-ui.html
                API Docs:   http://localhost:8080/v3/api-docs
                \s
                """);
    }
}
