package vinegaraflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import vinegaraflow.dto.LoginRequest;
import vinegaraflow.entity.Role;
import vinegaraflow.entity.User;
import vinegaraflow.repository.RoleRepository;
import vinegaraflow.repository.UserRepository;
import vinegaraflow.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SimpleControllerTest {

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    private MockMvc mvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(springSecurity())
                .build();
        mapper = new ObjectMapper();

        userRepo.deleteAll();
        if (roleRepo.findByName("ROLE_USER").isEmpty()) {
            roleRepo.save(Role.builder().name("ROLE_USER").build());
        }
    }

    @Test
    void testRegistration() throws Exception {
        String json = """
            {
                "username": "simpleuser",
                "email": "simple@test.com",
                "password": "12345"
            }
        """;

        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void testLogin() throws Exception {
        // Сначала создаём пользователя в БД
        Role userRole = roleRepo.findByName("ROLE_USER").orElseGet(() ->
                roleRepo.save(Role.builder().name("ROLE_USER").build()));

        User user = User.builder()
                .username("logintest")
                .email("login@test.com")
                .password(encoder.encode("pass123"))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        userRepo.save(user);

        LoginRequest login = new LoginRequest();
        login.setUsername("logintest");
        login.setPassword("pass123");

        mvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
