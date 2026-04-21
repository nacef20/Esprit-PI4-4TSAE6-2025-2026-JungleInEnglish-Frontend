package tn.esprit.forumservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.service.PostScoreProjection;

import java.util.List;
import java.util.Map;


@Repository
public interface PostRepo extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN p.reactions r " +
            "GROUP BY p " +
            "ORDER BY (COUNT(CASE WHEN r.type = 'LIKE' THEN 1 END)*2 + SIZE(p.comments)*3 - FUNCTION('DATEDIFF', CURRENT_DATE, p.createdAt)) DESC")
    List<Post> findTrendingPosts();


    @Query(value = """
    SELECT p.idpost AS idpost,
           p.authorid AS authorid,
           (
             COALESCE(r.scoreReaction,0) +
             COALESCE(c.commentsCount,0)*5
           ) AS score
    FROM post p

    
    LEFT JOIN (
        SELECT r.post_id,
               SUM(
                   CASE 
                       WHEN r.type = 'LIKE' THEN 15
                       WHEN r.type = 'LOVE' THEN 20
                       WHEN r.type = 'HAHA' THEN 10
                       
                       WHEN r.type = 'SAD' THEN 5
                       WHEN r.type = 'ANGRY' THEN -5
                       ELSE 0
                   END
               ) AS scoreReaction
        FROM reaction r
        JOIN post p2 ON r.post_id = p2.idpost
        WHERE r.user_id != p2.authorid 
        GROUP BY r.post_id
    ) r ON r.post_id = p.idpost

    -- 💬 COMMENTAIRES (SANS AUTEUR)
    LEFT JOIN (
        SELECT c.post_idpost AS post_id,
               COUNT(*) AS commentsCount
        FROM comment c
        JOIN post p3 ON c.post_idpost = p3.idpost
        WHERE c.authorid != p3.authorid -- 
          AND MONTH(c.date_send) = :month
          AND YEAR(c.date_send) = :year
        GROUP BY c.post_idpost
    ) c ON c.post_id = p.idpost

    WHERE MONTH(p.created_at) = :month 
      AND YEAR(p.created_at) = :year

    ORDER BY score DESC
    LIMIT 3
""", nativeQuery = true)
    List<Map<String, Object>> findTop3Posts(@Param("month") int month,
                                            @Param("year") int year);


}

