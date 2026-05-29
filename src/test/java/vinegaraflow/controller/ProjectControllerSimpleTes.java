package vinegaraflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vinegaraflow.entity.Project;
import vinegaraflow.entity.ProjectStatus;
import vinegaraflow.entity.Role;
import vinegaraflow.entity.User;
import vinegaraflow.repository.ProjectRepository;
import vinegaraflow.repository.RoleRepository;
import vinegaraflow.repository.UserRepository;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProjectControllerSimpleTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        projectRepository.deleteAll();
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        testUser = userRepository.save(User.builder()
                .username("projuser")
                .email("proj@test.com")
                .password("encoded")
                .enabled(true)
                .roles(Set.of(userRole))
                .build());
    }

    @Test
    void createProject() throws Exception {
        Project project = Project.builder()
                .name("Test Project")
                .description("Desc")
                .status(ProjectStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/projects")
                        .with(user("projuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void getAllProjects() throws Exception {
        projectRepository.save(Project.builder()
                .name("P1")
                .owner(testUser)
                .status(ProjectStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/projects")
                        .with(user("projuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("P1"));
    }

    @Test
    void updateProject() throws Exception {
        Project project = projectRepository.save(Project.builder()
                .name("Old")
                .owner(testUser)
                .status(ProjectStatus.ACTIVE)
                .build());

        Project update = Project.builder()
                .name("New")
                .description("Updated description")
                .status(ProjectStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/projects/{id}", project.getId())
                        .with(user("projuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void deleteProject() throws Exception {
        Project project = projectRepository.save(Project.builder()
                .name("ToDelete")
                .owner(testUser)
                .status(ProjectStatus.ACTIVE)
                .build());

        mockMvc.perform(delete("/api/projects/{id}", project.getId())
                        .with(user("projuser").roles("USER")))
                .andExpect(status().isOk());
    }
}
