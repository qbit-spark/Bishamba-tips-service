package org.qbitspark.bishambatipsservice;

import org.qbitspark.bishambatipsservice.authentication_service.entity.Roles;
import org.qbitspark.bishambatipsservice.authentication_service.repo.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;

@EnableAsync
@SpringBootApplication
public class BishambaTipsServiceApplication implements CommandLineRunner {

    @Autowired
    private RolesRepository roleRepository;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {return builder.build();}


    public static void main(String[] args) {
        SpringApplication.run(BishambaTipsServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ROLE_SUPER_ADMIN");
        createRoleIfNotExists("ROLE_FARMER");
        createRoleIfNotExists("ROLE_AGENT");
    }

    private void createRoleIfNotExists(String roleName) {
        Roles existingRole = roleRepository.findByRoleName(roleName).orElse(null);

        if (existingRole == null) {
            Roles newRole = new Roles();
            newRole.setRoleName(roleName);
            roleRepository.save(newRole);
        }
    }
}
