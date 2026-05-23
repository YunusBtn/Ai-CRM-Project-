package com.yunus.starter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Bu test gerçek PostgreSQL bağlantısı gerektirdiğinden unit test koşusunda disabled.
 * Integration test pipeline'ında çalıştırılabilir.
 */
@SpringBootTest
@Disabled("Gerçek DB bağlantısı gerektirir – sadece integration test ortamında çalıştırın")
class CrmProjectApplicationTests {

    @Test
    void contextLoads() {
    }

}
