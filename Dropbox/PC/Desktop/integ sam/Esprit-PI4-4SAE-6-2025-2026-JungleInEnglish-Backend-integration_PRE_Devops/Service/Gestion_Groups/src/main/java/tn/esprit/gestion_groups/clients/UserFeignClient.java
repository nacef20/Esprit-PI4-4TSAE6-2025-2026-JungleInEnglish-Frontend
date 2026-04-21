package tn.esprit.gestion_groups.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userservice")
public interface UserFeignClient {

    @GetMapping("/api/users/{id}")
    UtilisateurResumeDTO getUserById(@PathVariable("id") String id);

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    class UtilisateurResumeDTO {
        private String id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
    }
}