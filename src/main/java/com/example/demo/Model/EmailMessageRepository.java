package com.example.demo.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, Long> {
    
    // Find emails sent TO a user
    List<EmailMessage> findByReceiverEmailOrderBySentAtDesc(String receiverEmail);

    // Find emails sent BY a user
    List<EmailMessage> findBySenderEmailOrderBySentAtDesc(String senderEmail);

    @Query("SELECT e FROM EmailMessage e WHERE (e.receiverEmail = :email1 AND e.senderEmail = :email2) OR (e.senderEmail = :email1 AND e.receiverEmail = :email2) ORDER BY e.sentAt DESC")
    List<EmailMessage> findConversation(@Param("email1") String email1, @Param("email2") String email2);

    List<EmailMessage> findByParentEmailIdOrderBySentAtAsc(Long parentEmailId);
}
