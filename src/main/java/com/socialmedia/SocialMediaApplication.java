package com.socialmedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SocialMediaApplication.java
 * ══════════════════════════════════════════════════
 * Spring Boot entry point.
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * This auto-discovers all @RestController, @Service, @Component beans.
 */
@SpringBootApplication
public class SocialMediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialMediaApplication.class, args);
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║   🌐  Social Media Engine — Spring Boot API     ║");
        System.out.println("║   http://localhost:8080                         ║");
        System.out.println("║   Open index.html in your browser               ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
    }
}