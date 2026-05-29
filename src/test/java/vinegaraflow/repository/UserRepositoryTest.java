package vinegaraflow.repository;

import org.springframework.test.context.ActiveProfiles;
import vinegaraflow.entity.Role;
import vinegaraflow.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName("ROLE_USER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_USER").build()));

        testUser = User.builder()
                .username("repotest")
                .email("repo@example.com")
                .password("encodedpass")
                .enabled(true)
                .build();
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);
    }

    @Test
    void testFindByUsername_Success() {
        Optional<User> found = userRepository.findByUsername("repotest");
        assertTrue(found.isPresent());
        assertEquals("repotest", found.get().getUsername());
    }

    @Test
    void testFindByUsername_NotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername_True() {
        boolean exists = userRepository.existsByUsername("repotest");
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_False() {
        boolean exists = userRepository.existsByUsername("fakeuser");
        assertFalse(exists);
    }

    @Test
    void testSaveUser() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpass")
                .enabled(true)
                .build();
        newUser.setRoles(Set.of(userRole));
        User saved = userRepository.save(newUser);
        assertNotNull(saved.getId());
        assertEquals("newuser", saved.getUsername());
    }

    @Test
    void testDeleteUser() {
        userRepository.delete(testUser);
        Optional<User> found = userRepository.findByUsername("repotest");
        assertFalse(found.isPresent());
    }
}