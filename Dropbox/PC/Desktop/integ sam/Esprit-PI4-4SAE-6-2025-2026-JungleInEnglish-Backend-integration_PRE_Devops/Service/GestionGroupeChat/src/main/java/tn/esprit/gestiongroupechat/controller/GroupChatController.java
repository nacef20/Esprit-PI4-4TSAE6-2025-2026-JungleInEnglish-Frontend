package tn.esprit.gestiongroupechat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.gestiongroupechat.Dto.GroupChatDTO;
import tn.esprit.gestiongroupechat.entities.GroupChat;
import tn.esprit.gestiongroupechat.service.GroupChatService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController
@RequestMapping("/chatPrivee")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final String uploadDir = "C:/Users/chiri/IdeaProjects/jungle-in-english-backend-nacef/Service/GestionGroupeChat/uploads/images/";
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

    @PostMapping("/add")
    public ResponseEntity<GroupChatDTO> creategroup(@RequestBody @Valid GroupChatDTO request){
        GroupChatDTO created = groupChatService.creategroup(request);
        return ResponseEntity.ok(created);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<GroupChatDTO> updateGroup(@PathVariable Long id,
                                                    @RequestBody @Valid GroupChatDTO request) {
        GroupChatDTO updated = groupChatService.updateGroup(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long id) {
        groupChatService.deleteGroup(id);
        return ResponseEntity.ok("Groupe supprimé avec succès");
    }



    @GetMapping("/all")
    public ResponseEntity<List<GroupChatDTO>> getAllGroups() {
        List<GroupChatDTO> groups = groupChatService.getAllGroups();
        return ResponseEntity.ok(groups);
    }


    @GetMapping("/{id}")
    public ResponseEntity<GroupChatDTO> getGroupById(@PathVariable Long id) {
        GroupChatDTO group = groupChatService.getGroupById(id);
        return ResponseEntity.ok(group);
    }


}
