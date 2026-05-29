package vinegaraflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails user = User.builder()
                .username("testuser")
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void testValidateTokenSuccess() {
        UserDetails user = User.builder()
                .username("validuser")
                .password("")
                .authorities(List.of())
                .build();
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.validateToken(token, user));
    }

    @Test
    void testValidateTokenFailsForDifferentUser() {
        UserDetails user1 = User.builder().username("user1").password("").build();
        UserDetails user2 = User.builder().username("user2").password("").build();
        String token = jwtService.generateToken(user1);
        assertFalse(jwtService.validateToken(token, user2));
    }
}