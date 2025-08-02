package com.chatapp.repository;

import com.chatapp.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByFileType(String fileType);
    
    @Query("SELECT a FROM Attachment a JOIN a.messages m WHERE m.id = ?1")
    List<Attachment> findByMessageId(Long messageId);
    
    @Query("SELECT a FROM Attachment a JOIN a.messages m WHERE m.chat.id = ?1")
    List<Attachment> findByChatId(Long chatId);
    
    @Query("SELECT a FROM Attachment a JOIN a.messages m WHERE m.sender.id = ?1")
    List<Attachment> findByUserId(Long userId);
    
    @Query("SELECT a FROM Attachment a WHERE a.type = ?1 AND EXISTS (SELECT m FROM a.messages m WHERE m.chat.id = ?2)")
    List<Attachment> findByTypeAndChatId(String attachmentType, Long chatId);
}