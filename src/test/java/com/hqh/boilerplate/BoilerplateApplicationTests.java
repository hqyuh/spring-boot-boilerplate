package com.hqh.boilerplate;

import com.hqh.boilerplate.BoilerplateApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BoilerplateApplication.class)
@ActiveProfiles("test")
class BoilerplateApplicationTests {

    @Test
    void contextLoads() {
    }

}
