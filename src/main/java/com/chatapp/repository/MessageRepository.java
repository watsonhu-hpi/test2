package com.chatapp.repository;

import com.chatapp.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);
    
    List<Message> findByChatIdAndCreatedAtAfterOrderByCreatedAtAsc(Long chatId, LocalDateTime after);
    
    List<Message> findBySenderId(Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = ?1 AND m.content LIKE %?2%")
    List<Message> searchMessagesInChat(Long chatId, String keyword);
    
    @Query("SELECT m FROM Message m JOIN m.readBy r WHERE m.chat.id = ?1 AND r.id = ?2")
    List<Message> findMessagesReadByUser(Long chatId, Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = ?1 AND m.id NOT IN (SELECT rm.id FROM Message rm JOIN rm.readBy r WHERE r.id = ?2)")
    List<Message> findUnreadMessagesForUser(Long chatId, Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = ?1 AND m.id NOT IN (SELECT rm.id FROM Message rm JOIN rm.readBy r WHERE r.id = ?2)")
    Long countUnreadMessagesForUser(Long chatId, Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.content LIKE %?1% AND m.chat.id IN (SELECT c.id FROM Chat c JOIN c.members mem WHERE mem.id = ?2)")
    List<Message> searchMessagesForUser(String keyword, Long userId);
}