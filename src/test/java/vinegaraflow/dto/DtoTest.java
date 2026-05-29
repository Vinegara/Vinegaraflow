package vinegaraflow.dto;

import vinegaraflow.entity.Priority;
import vinegaraflow.entity.ProjectRole;
import vinegaraflow.entity.TaskStatus;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testLoginRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        String json = mapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("testuser"));

        LoginRequest deserialized = mapper.readValue(json, LoginRequest.class);
        assertEquals("testuser", deserialized.getUsername());
        assertEquals("password", deserialized.getPassword());
    }

    @Test
    void testSignupRequest() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("secret");

        String json = mapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("newuser"));

        SignupRequest deserialized = mapper.readValue(json, SignupRequest.class);
        assertEquals("newuser", deserialized.getUsername());
        assertEquals("new@test.com", deserialized.getEmail());
        assertEquals("secret", deserialized.getPassword());
    }

    @Test
    void testTaskRequest() throws Exception {
        TaskRequest request = new TaskRequest("Task title", "Description", TaskStatus.TODO, Priority.HIGH, LocalDate.now(), 1L, 2L);

        String json = mapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("Task title"));

        TaskRequest deserialized = mapper.readValue(json, TaskRequest.class);
        assertEquals("Task title", deserialized.title());
        assertEquals("Description", deserialized.description());
    }

    @Test
    void testCommentRequest() throws Exception {
        CommentRequest request = new CommentRequest("Great comment!", 1L);

        String json = mapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("Great comment!"));

        CommentRequest deserialized = mapper.readValue(json, CommentRequest.class);
        assertEquals("Great comment!", deserialized.text());
        assertEquals(1L, deserialized.taskId());
    }

    @Test
    void testNotificationRequest() throws Exception {
        NotificationRequest request = new NotificationRequest(1L, "Hello", "INFO");

        String json = mapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("Hello"));

        NotificationRequest deserialized = mapper.readValue(json, NotificationRequest.class);
        assertEquals("Hello", deserialized.message());
        assertEquals("INFO", deserialized.type());
    }

    @Test
    void testProjectMemberRequest() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(1L, 2L, ProjectRole.DEVELOPER);

        String json = mapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("DEVELOPER"));

        ProjectMemberRequest deserialized = mapper.readValue(json, ProjectMemberRequest.class);
        assertEquals(ProjectRole.DEVELOPER, deserialized.role());
    }
}