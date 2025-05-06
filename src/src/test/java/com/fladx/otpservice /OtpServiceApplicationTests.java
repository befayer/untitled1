package com.fladx.otpservice;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.dto.otp.GenerateCodeRequestDto;
import com.fladx.otpservice.dto.otp.ValidateCodeRequestDto;
import com.fladx.otpservice.model.user.UserRole;
import com.fladx.otpservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OtpServiceApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager; // Для проверки типа БД

    @BeforeEach
    void cleanupDatabase() {
        userRepository.deleteAll(); // Очищаем БД перед каждым тестом
    }

    // Тест 1: Проверка загрузки контекста Spring
    @Test
    void contextLoads() {
        assertNotNull(restTemplate, "Spring контекст должен загружаться");
        assertNotNull(userRepository, "Репозиторий должен быть доступен");
    }

    // Тест 2: Проверка подключения к тестовой БД (H2)
    @Test
    void testDatabaseConnection() {
        assertDoesNotThrow(() -> {
            long count = userRepository.count();
            System.out.println("Количество записей в БД: " + count);
        }, "Подключение к тестовой БД должно работать");
    }

    // Тест 3: Проверка что используется H2
    @Test
    void testH2DatabaseIsUsed() throws Exception {
        String dbUrl = entityManager.getEntityManagerFactory()
                .getProperties()
                .get("hibernate.connection.url").toString();

        assertTrue(dbUrl.contains("h2:mem"),
                "Должна использоваться H2 in-memory база, а используется: " + dbUrl);
    }

    // Тест 4: Проверка невозможности создания второго администратора
    @Test
    void shouldNotCreateSecondAdmin() {
        // Первый администратор создается успешно
        ResponseEntity<String> firstAdminResponse = registerUser(UserRole.ADMIN);
        assertEquals(HttpStatus.OK, firstAdminResponse.getStatusCode());

        // Попытка создать второго администратора
        ResponseEntity<String> secondAdminResponse = registerUser(UserRole.ADMIN);
        assertEquals(HttpStatus.BAD_REQUEST, secondAdminResponse.getStatusCode());
        assertTrue(secondAdminResponse.getBody().contains("Admin user already exists"));
    }

    // Тест 5: Полный цикл работы с OTP
    @Test
    void shouldGenerateAndValidateOtp() {
        // 1. Регистрация пользователя
        registerUser(UserRole.USER);

        // 2. Аутентификация и получение токена
        String token = loginUser();
        assertNotNull(token, "Токен не должен быть null");

        // 3. Генерация OTP-кода
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        GenerateCodeRequestDto generateRequest = new GenerateCodeRequestDto("payment");
        ResponseEntity<String> generateResponse = restTemplate.exchange(
                "/otp",
                HttpMethod.POST,
                new HttpEntity<>(generateRequest, headers),
                String.class);

        assertEquals(HttpStatus.OK, generateResponse.getStatusCode());
        String otpCode = generateResponse.getBody();
        assertNotNull(otpCode, "OTP-код не должен быть null");

        // 4. Валидация OTP-кода
        ValidateCodeRequestDto validateRequest = new ValidateCodeRequestDto(otpCode);
        ResponseEntity<Void> validateResponse = restTemplate.exchange(
                "/otp/validate",
                HttpMethod.POST,
                new HttpEntity<>(validateRequest, headers),
                Void.class);

        assertEquals(HttpStatus.OK, validateResponse.getStatusCode());
    }

    // Вспомогательный метод для регистрации пользователя
    private ResponseEntity<String> registerUser(UserRole role) {
        UserDto userDto = new UserDto(
                "testuser_" + role.name() + "_" + System.currentTimeMillis(),
                "password123",
                role);
        return restTemplate.postForEntity("/auth/register", userDto, String.class);
    }

    // Вспомогательный метод для аутентификации
    private String loginUser() {
        UserDto userDto = new UserDto(
                "testuser_USER_" + System.currentTimeMillis(),
                "password123",
                UserRole.USER);
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/login", userDto, String.class);
        return response.getBody();
    }
}