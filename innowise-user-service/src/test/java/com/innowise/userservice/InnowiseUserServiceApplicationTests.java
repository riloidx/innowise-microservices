package com.innowise.userservice;

import com.innowise.userservice.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InnowiseUserServiceApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
    }

}
