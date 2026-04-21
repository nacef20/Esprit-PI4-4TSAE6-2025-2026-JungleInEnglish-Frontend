package tn.esprit.jungleinenglishbackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import tn.esprit.jungleinenglishbackend.Services.OrderService;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "app.security.mock-mode=false" })
@AutoConfigureMockMvc
public class OrderTotalMoneyJwtModeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    public void testGetTotalMoneyForCurrentUser_JwtMode() throws Exception {
        when(orderService.getTotalMoneyForCurrentUser()).thenReturn(new BigDecimal("1234.56"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/total-money")
                .with(jwt().jwt(builder -> builder.subject("jwt-user-456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMoney").value(1234.56));
    }

    @Test
    public void testGetTotalMoneyForAllUsers_AdminRequired_JwtMode() throws Exception {
        when(orderService.getTotalMoneyForAllUsers()).thenReturn(new BigDecimal("9999.99"));

        // User should be forbidden (missing realm_access ADMIN role)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/orders/total-money")
                .with(jwt().jwt(builder -> builder.subject("jwt-user-456"))))
                .andExpect(status().isForbidden());

        // Admin should be allowed
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/orders/total-money")
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMoney").value(9999.99));
    }
}
