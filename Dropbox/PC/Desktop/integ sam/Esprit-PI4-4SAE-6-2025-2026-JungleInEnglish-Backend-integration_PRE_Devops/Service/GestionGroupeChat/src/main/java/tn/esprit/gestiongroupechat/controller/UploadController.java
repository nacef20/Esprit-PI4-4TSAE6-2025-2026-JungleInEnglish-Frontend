package tn.esprit.gestiongroupechat.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final String uploadDir = "C:/managment-jungle-bakend/Service/GestionGroupeChat/uploads/images/";
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Aucun fichier sélectionné");
        }

        try {
            // Crée le dossier s'il n'existe pas
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Nom du fichier avec timestamp pour éviter collisions
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir + filename);

            // Sauvegarde fichier
            file.transferTo(dest);

            // Retourne URL relative pour le front
            String fileUrl = "/uploads/images/" + filename;
            return ResponseEntity.ok().body(new ImageResponse(fileUrl));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload");
        }
    }

    // Classe pour retourner JSON { "url": "/uploads/images/xxx.jpg" }
    static class ImageResponse {
        private String url;
        public ImageResponse(String url) { this.url = url; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
