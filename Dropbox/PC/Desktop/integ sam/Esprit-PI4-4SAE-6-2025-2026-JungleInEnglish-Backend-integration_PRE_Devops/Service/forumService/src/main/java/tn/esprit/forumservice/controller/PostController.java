package tn.esprit.forumservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.forumservice.dto.PostDTO;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.ollama.Ollamaservice;
import tn.esprit.forumservice.service.PostIn;
import tn.esprit.forumservice.service.PostScoreProjection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")


@RequiredArgsConstructor
public class PostController {
    private final PostIn postService;
    private final Ollamaservice ollamaService;


    @GetMapping("/user/{id}")
    public Map<String, Object> getUser(@PathVariable String id) {
        return postService.getUser(id);
    }

    @PostMapping("/add")
    public ResponseEntity<PostDTO> createPost(@RequestBody @Valid PostDTO request) {
        PostDTO created = postService.createPost(request);
        System.out.println("authorid: " + request.authorid()); // ← qu'est ce qui s'affiche ?

        return ResponseEntity.ok(created);
    }

    // 🔹 GET ALL
    @GetMapping("/all")
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // 🔹 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // 🔹 UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostDTO request) {

        PostDTO updated = postService.updatePost(id, request);
        return ResponseEntity.ok(updated);
    }

    // 🔹 DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }
    private final String uploadDir = "C:/jungle-in-english-backend-integration1/Service/forumService/src/main/resources/imageVedioPost/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Générer un nom unique pour éviter les collisions
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + filename);
            Files.copy(file.getInputStream(), filePath);

            // Retourne juste le nom de fichier pour le stocker en base
            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload");
        }
    }

    // ✅ 1. Correction
    @PostMapping("/correct")
    public ResponseEntity<String> correct(@RequestBody String text) {
        String result = ollamaService.correctText(text);
        return ResponseEntity.ok(result);
    }

    // ✅ 2. Résumé
    @PostMapping("/summarize")
    public ResponseEntity<String> summarize(@RequestBody String text) {
        String result = ollamaService.summarizeText(text);
        return ResponseEntity.ok(result);
    }

    // ✅ 3. Assistant
    @PostMapping("/assist")
    public ResponseEntity<String> assist(@RequestBody String text) {
        String result = ollamaService.askAssistant(text);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/trending")
    public List<Post> getTrendingPosts() {
        return postService.getTrendingPosts();
    }



    @GetMapping("/top3/{month}/{year}")
    public ResponseEntity<List<Map<String, Object>>> getTop3(@PathVariable int month, @PathVariable int year) {
        return ResponseEntity.ok(postService.getTop3Posts(month, year));
    }


}
