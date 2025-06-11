package kr.hhplus.be.server.reservation.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public abstract class IntegrationTestSupport {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Container
    @ServiceConnection
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);
}
