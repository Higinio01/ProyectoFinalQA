package org.example;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "org.example.stepdefinitions",
        plugin = {"pretty", "html:build/reports/cucumber-report.html"},
        monochrome = true
)
@ActiveProfiles("test")
public class CucumberTest {
}
