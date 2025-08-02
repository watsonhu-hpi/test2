package com.chatapp.repository;

import com.chatapp.model.Chat;
import com.chatapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.members m WHERE m.id = ?1")
    List<Chat> findByMemberId(Long userId);
    
    @Query("SELECT c FROM Chat c JOIN c.members m WHERE m.id = ?1")
    Page<Chat> findByMemberId(Long userId, Pageable pageable);
    
    @Query("SELECT c FROM Chat c WHERE c.type = 'DIRECT' AND ?1 IN (SELECT m.id FROM c.members m) AND ?2 IN (SELECT m.id FROM c.members m)")
    Optional<Chat> findDirectChatBetweenUsers(Long user1Id, Long user2Id);
    
    @Query("SELECT c FROM Chat c WHERE c.name LIKE %?1% AND ?2 IN (SELECT m.id FROM c.members m)")
    List<Chat> searchChatsByNameForUser(String keyword, Long userId);
    
    @Query("SELECT c FROM Chat c WHERE c.type = ?1 AND ?2 IN (SELECT m.id FROM c.members m)")
    List<Chat> findChatsByTypeForUser(String chatType, Long userId);
    
    @Query("SELECT c FROM Chat c WHERE c.creator.id = ?1")
    List<Chat> findChatsByCreator(Long userId);
}