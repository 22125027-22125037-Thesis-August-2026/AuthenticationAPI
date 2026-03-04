package com.mhsa.backend.auth.config;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.mhsa.backend.auth.service.CustomUserDetailsService;
import com.mhsa.backend.auth.service.TokenBlacklistService;
import com.mhsa.backend.auth.utils.JwtUtils;

@SpringBootTest(properties = {
    "mhsa.app.jwtSecret=QWJjZGVmR2hpamtsTW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwQUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo=",
    "mhsa.app.jwtExpirationMs=3600000"
})
@SuppressWarnings("unused")
class JwtAuthenticationFilterIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private AtomicReference<Object> capturedPrincipal;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        capturedPrincipal.set(null);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetUuidPrincipalInSecurityContext_whenTokenSubjectIsUuid() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtUtils.generateToken(userId, "user@example.com");

        UserDetails userDetails = new User("user@example.com", "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/tracking/test/security-principal")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());

        assertEquals(userId.toString(), capturedPrincipal.get());
    }

    @Test
    void shouldDenyAccessAndLeaveSecurityContextEmpty_whenTokenIsInvalid() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/tracking/test/security-principal")
                .header("Authorization", "Bearer invalid.token.value")
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().is4xxClientError())
                .andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertTrue(status == 401 || status == 403);

        assertNull(capturedPrincipal.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @TestConfiguration
    static class TestEndpointConfiguration {

        @Bean
        AtomicReference<Object> capturedPrincipal() {
            return new AtomicReference<>();
        }

        @RestController
        @RequestMapping("/api/v1/tracking/test")
        static class TestSecurityProbeController {

            private final AtomicReference<Object> capturedPrincipal;

            TestSecurityProbeController(AtomicReference<Object> capturedPrincipal) {
                this.capturedPrincipal = capturedPrincipal;
            }

            @GetMapping("/security-principal")
            String readSecurityPrincipal() {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                capturedPrincipal.set(principal);
                return principal.toString();
            }
        }
    }
}
