package com.example.demo;

import com.example.demo.Model.AddressBookContact;
import com.example.demo.Model.AddressBookContactRepository;
import com.example.demo.Model.EmailMessage;
import com.example.demo.Model.EmailMessageRepository;
import com.example.demo.Model.MyAppUser;
import com.example.demo.Model.MyAppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Sprint2IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MyAppUserRepository userRepository;

    @Autowired
    private AddressBookContactRepository contactRepository;

    @Autowired
    private EmailMessageRepository emailRepository;

    private MyAppUser testUserA;
    private MyAppUser testUserB;

    @BeforeEach
    public void setup() {
        // Create Test User A
        testUserA = new MyAppUser();
        testUserA.setUsername("UserA");
        testUserA.setEmail("usera@test.com");
        testUserA.setPassword("password12345");
        testUserA.setVerified(true);
        userRepository.save(testUserA);

        // Create Test User B
        testUserB = new MyAppUser();
        testUserB.setUsername("UserB");
        testUserB.setEmail("userb@test.com");
        testUserB.setPassword("password12345");
        testUserB.setVerified(true);
        userRepository.save(testUserB);
    }

    // ============================================================
    // TC-01 & TC-03: Create Contact and Display Check
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testCreateAndDisplayContact_TC01_TC03() throws Exception {
        // TC-03: Create a new contact
        mockMvc.perform(post("/address-book/save")
                .with(csrf())
                .param("name", "John Doe")
                .param("email", "john@example.com")
                .param("contactInformation", "Friend from college"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email"));

        // Verify the contact was persisted
        List<AddressBookContact> contacts = contactRepository.findByUserId(testUserA.getId());
        assertEquals(1, contacts.size());
        assertEquals("John Doe", contacts.get(0).getName());

        // TC-01: Displaying Available Contacts
        mockMvc.perform(get("/address-book"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("contacts"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("John Doe")));
    }

    // ============================================================
    // TC-02: Contact Details View
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testViewContactDetails_TC02() throws Exception {
        // Pre-condition: contact exists
        AddressBookContact contact = new AddressBookContact();
        contact.setUserId(testUserA.getId());
        contact.setName("Jane Smith");
        contact.setEmail("jane@example.com");
        contact.setContactInformation("Work colleague");
        contact = contactRepository.save(contact);

        // Click on item to show more details
        mockMvc.perform(get("/address-book/" + contact.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Jane Smith")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jane@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Work colleague")));
    }

    // ============================================================
    // TC-04: Update Contact
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testUpdateContact_TC04() throws Exception {
        // Pre-condition: existing contact
        AddressBookContact contact = new AddressBookContact();
        contact.setUserId(testUserA.getId());
        contact.setName("Old Name");
        contact.setEmail("old@test.com");
        contact = contactRepository.save(contact);

        // Perform update
        mockMvc.perform(post("/address-book/update/" + contact.getId())
                .with(csrf())
                .param("name", "New Name")
                .param("email", "new@test.com")
                .param("contactInformation", "Updated details"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email"));

        // Verify update persisted
        AddressBookContact updated = contactRepository.findById(contact.getId()).get();
        assertEquals("New Name", updated.getName());
        assertEquals("new@test.com", updated.getEmail());
    }

    // ============================================================
    // TC-05: Delete Contact
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testDeleteContact_TC05() throws Exception {
        // Pre-condition: existing contact
        AddressBookContact contact = new AddressBookContact();
        contact.setUserId(testUserA.getId());
        contact.setName("To Delete");
        contact = contactRepository.save(contact);

        // Process Contact -> Delete
        mockMvc.perform(post("/address-book/delete/" + contact.getId())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email"));

        // Verify deletion
        assertTrue(contactRepository.findById(contact.getId()).isEmpty());
    }

    // ============================================================
    // TC-06: Data Isolation - Separate contact lists per user
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testContactDataIsolation_TC06() throws Exception {
        // User A adds a contact
        AddressBookContact contactA = new AddressBookContact();
        contactA.setUserId(testUserA.getId());
        contactA.setName("Contact X");
        contactA.setEmail("x@example.com");
        contactRepository.save(contactA);

        // User B should have zero contacts
        List<AddressBookContact> contactsB = contactRepository.findByUserId(testUserB.getId());
        assertTrue(contactsB.isEmpty(), "User B should not see User A's contacts");

        // User A should have exactly 1
        List<AddressBookContact> contactsA = contactRepository.findByUserId(testUserA.getId());
        assertEquals(1, contactsA.size());
    }

    // ============================================================
    // TC-07: Email Inbox Display
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testEmailInboxDisplay_TC07() throws Exception {
        // Pre-condition: User A has received an email
        EmailMessage msg = new EmailMessage();
        msg.setSenderId(testUserB.getId());
        msg.setSenderEmail(testUserB.getEmail());
        msg.setReceiverEmail(testUserA.getEmail());
        msg.setSubject("Hello from B");
        msg.setBody("Test body");
        msg.setSentAt(LocalDateTime.now());
        emailRepository.save(msg);

        // Click Email Mode tab -> see inbox
        mockMvc.perform(get("/email"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("inboxEmails"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello from B")));
    }

    // ============================================================
    // TC-08: Search Function
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testSearchFunction_TC08() throws Exception {
        // Pre-condition: Multiple emails in inbox
        EmailMessage msg1 = new EmailMessage();
        msg1.setSenderId(testUserB.getId());
        msg1.setSenderEmail(testUserB.getEmail());
        msg1.setReceiverEmail(testUserA.getEmail());
        msg1.setSubject("Important Meeting");
        msg1.setBody("Let's meet tomorrow.");
        msg1.setSentAt(LocalDateTime.now());
        emailRepository.save(msg1);

        EmailMessage msg2 = new EmailMessage();
        msg2.setSenderId(testUserB.getId());
        msg2.setSenderEmail("other@test.com");
        msg2.setReceiverEmail(testUserA.getEmail());
        msg2.setSubject("Unrelated Topic");
        msg2.setBody("Some other email.");
        msg2.setSentAt(LocalDateTime.now());
        emailRepository.save(msg2);

        // Search by sender email
        mockMvc.perform(get("/email").param("search", "userb@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Important Meeting")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Unrelated Topic"))));
    }

    // ============================================================
    // TC-09 & TC-10: Compose Email & Inbox Sync
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testComposeEmailAndInboxSync_TC09_TC10() throws Exception {
        // TC-09: User A composes and sends an email to User B
        mockMvc.perform(post("/email/send")
                .with(csrf())
                .param("receiverEmail", testUserB.getEmail())
                .param("subject", "Hello User B")
                .param("body", "This is a test message from A to B."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email"));

        // TC-10: Verify it appears in User B's inbox
        List<EmailMessage> inboxB = emailRepository.findByReceiverEmailOrderBySentAtDesc(testUserB.getEmail());
        assertEquals(1, inboxB.size());
        EmailMessage sentMsg = inboxB.get(0);
        assertEquals("Hello User B", sentMsg.getSubject());
        assertEquals(testUserA.getEmail(), sentMsg.getSenderEmail());
    }

    // ============================================================
    // TC-11: Email Threading and View Trail
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testEmailThreadingAndView_TC11() throws Exception {
        // Pre-condition: User A received an email from User B
        EmailMessage msg = new EmailMessage();
        msg.setSenderId(testUserB.getId());
        msg.setSenderEmail(testUserB.getEmail());
        msg.setReceiverEmail(testUserA.getEmail());
        msg.setSubject("Question for User A");
        msg.setBody("How are you doing today?");
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);
        msg = emailRepository.save(msg);

        // View the email thread
        mockMvc.perform(get("/email/view/" + msg.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("trail"))
                .andExpect(model().attributeExists("reply"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("How are you doing today?")));

        // Email should be automatically marked read
        EmailMessage reloaded = emailRepository.findById(msg.getId()).get();
        assertTrue(reloaded.isRead());
    }

    // ============================================================
    // TC-12: Quick Reply
    // ============================================================
    @Test
    @WithMockUser(username = "usera@test.com")
    public void testQuickReply_TC12() throws Exception {
        // Pre-condition: an email from User B to User A
        EmailMessage original = new EmailMessage();
        original.setSenderId(testUserB.getId());
        original.setSenderEmail(testUserB.getEmail());
        original.setReceiverEmail(testUserA.getEmail());
        original.setSubject("Original Subject");
        original.setBody("Original message body.");
        original.setSentAt(LocalDateTime.now());
        original = emailRepository.save(original);

        // User A sends a reply
        mockMvc.perform(post("/email/send")
                .with(csrf())
                .param("receiverEmail", testUserB.getEmail())
                .param("subject", "Re: Original Subject")
                .param("body", "This is my reply!")
                .param("parentEmailId", original.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email"));

        // Verify reply stored correctly
        List<EmailMessage> inboxB = emailRepository.findByReceiverEmailOrderBySentAtDesc(testUserB.getEmail());
        assertEquals(1, inboxB.size());
        assertEquals("Re: Original Subject", inboxB.get(0).getSubject());
        assertEquals("This is my reply!", inboxB.get(0).getBody());
    }
}
