package tn.esprit.forumservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "userservice", url = "http://localhost:2020")
public interface UserClient {
    @GetMapping("/users/{id}")
    Map<String, Object> getUserById(@PathVariable String id);
}
