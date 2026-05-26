package hr.beatsync.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jedinični testovi generiranja i validacije JWT tokena.
 * Klasa se instancira izravno (bez Spring konteksta i baze).
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        // Tajni ključ mora imati barem 32 bajta (HS256).
        tokenProvider = new JwtTokenProvider(
                "test-tajni-kljuc-mora-imati-min-32-znaka-123456", 3_600_000L);
    }

    @Test
    @DisplayName("Generirani token je valjan")
    void generiraniToken_jeValjan() {
        String token = tokenProvider.generateToken("dj_x", "IZVODAC");
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Iz tokena se ispravno čita korisničko ime")
    void citaUsernameIzTokena() {
        String token = tokenProvider.generateToken("dj_x", "IZVODAC");
        assertEquals("dj_x", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Iz tokena se ispravno čita uloga")
    void citaUloguIzTokena() {
        String token = tokenProvider.generateToken("klub_x", "BUSINESS");
        assertEquals("BUSINESS", tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Neispravan token nije valjan")
    void neispravanToken_nijeValjan() {
        assertFalse(tokenProvider.validateToken("ovo.nije.validan.token"));
    }
}
