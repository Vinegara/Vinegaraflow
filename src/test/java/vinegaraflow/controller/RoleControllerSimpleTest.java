package vinegaraflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import vinegaraflow.entity.Role;
import vinegaraflow.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoleControllerSimpleTest {

    @Autowired
    private RoleController roleController;

    @Autowired
    private RoleRepository roleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        roleRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole() throws Exception {
        Role role = Role.builder().name("ROLE_MANAGER").build();
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ROLE_MANAGER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRoles() throws Exception {
        Role role1 = Role.builder().name("ROLE_READER").build();
        Role role2 = Role.builder().name("ROLE_WRITER").build();
        roleRepository.save(role1);
        roleRepository.save(role2);

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole() throws Exception {
        Role role = Role.builder().name("ROLE_OLD").build();
        role = roleRepository.save(role);

        Role update = Role.builder().name("ROLE_NEW").build();
        mockMvc.perform(put("/api/admin/roles/{id}", role.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ROLE_NEW"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole() throws Exception {
        Role role = Role.builder().name("ROLE_TEMP").build();
        role = roleRepository.save(role);

        mockMvc.perform(delete("/api/admin/roles/{id}", role.getId()))
                .andExpect(status().isOk());
    }
}