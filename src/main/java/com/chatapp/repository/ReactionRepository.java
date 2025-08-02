package com.chatapp.repository;

import com.chatapp.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    @Query("SELECT r FROM Reaction r JOIN r.messages m WHERE m.id = ?1")
    List<Reaction> findByMessageId(Long messageId);
    
    @Query("SELECT r FROM Reaction r JOIN r.messages m WHERE m.id = ?1 AND r.user.id = ?2")
    List<Reaction> findByMessageIdAndUserId(Long messageId, Long userId);
    
    @Query("SELECT COUNT(r) FROM Reaction r JOIN r.messages m WHERE m.id = ?1 AND r.emoji = ?2")
    Long countByMessageIdAndEmoji(Long messageId, String emoji);
    
    @Query("SELECT r.emoji, COUNT(r) FROM Reaction r JOIN r.messages m WHERE m.id = ?1 GROUP BY r.emoji")
    List<Object[]> countReactionsByMessageId(Long messageId);
}