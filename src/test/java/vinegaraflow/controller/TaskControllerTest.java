package vinegaraflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vinegaraflow.dto.TaskRequest;
import vinegaraflow.entity.Priority;
import vinegaraflow.entity.Project;
import vinegaraflow.entity.ProjectStatus;
import vinegaraflow.entity.Role;
import vinegaraflow.entity.Task;
import vinegaraflow.entity.TaskStatus;
import vinegaraflow.entity.User;
import vinegaraflow.repository.ProjectRepository;
import vinegaraflow.repository.RoleRepository;
import vinegaraflow.repository.TaskRepository;
import vinegaraflow.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = roleRepository.save(Role.builder()
                .name("ROLE_USER")
                .build());

        testUser = userRepository.save(User.builder()
                .username("taskuser")
                .email("task@test.com")
                .password(passwordEncoder.encode("pass"))
                .enabled(true)
                .roles(Set.of(userRole))
                .build());

        testProject = projectRepository.save(Project.builder()
                .name("Project")
                .owner(testUser)
                .status(ProjectStatus.ACTIVE)
                .build());
    }

    @Test
    void createTask() throws Exception {
        TaskRequest request = new TaskRequest(
                "New Task",
                "Desc",
                TaskStatus.TODO,
                Priority.MEDIUM,
                LocalDate.now().plusDays(7),
                testProject.getId(),
                null
        );

        mockMvc.perform(post("/api/tasks")
                        .with(user("taskuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    void getAllTasks() throws Exception {
        taskRepository.save(Task.builder()
                .title("Task1")
                .project(testProject)
                .createdBy(testUser)
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .build());

        mockMvc.perform(get("/api/tasks")
                        .with(user("taskuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task1"));
    }

    @Test
    void updateTask() throws Exception {
        Task task = taskRepository.save(Task.builder()
                .title("Old")
                .project(testProject)
                .createdBy(testUser)
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .build());

        TaskRequest update = new TaskRequest(
                "Updated",
                null,
                TaskStatus.IN_PROGRESS,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .with(user("taskuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteTask() throws Exception {
        Task task = taskRepository.save(Task.builder()
                .title("ToDelete")
                .project(testProject)
                .createdBy(testUser)
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .build());

        mockMvc.perform(delete("/api/tasks/{id}", task.getId())
                        .with(user("taskuser").roles("USER")))
                .andExpect(status().isOk());
    }
}