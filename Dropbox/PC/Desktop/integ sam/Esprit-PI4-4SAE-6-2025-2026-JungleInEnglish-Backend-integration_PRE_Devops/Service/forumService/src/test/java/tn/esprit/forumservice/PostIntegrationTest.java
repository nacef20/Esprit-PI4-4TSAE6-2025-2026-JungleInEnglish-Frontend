package tn.esprit.forumservice;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.repository.PostRepo;
import tn.esprit.forumservice.service.PostService;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
public class PostIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepo postRepo;

    @Test
    void getTop3Posts_shouldReturnDataFromDB() {



        // 🔹 2. Call service
        var result = postService.getTop3Posts(4, 2026);

        // 🔹 3. Assert
        assertNotNull(result);
        // selon ton SQL
        assertFalse(result.isEmpty());
    }
}
