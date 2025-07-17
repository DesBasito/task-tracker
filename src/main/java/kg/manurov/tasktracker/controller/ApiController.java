package kg.manurov.tasktracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.manurov.tasktracker.service.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/external")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для регистрации нового пользователя")
public class ApiController {
    private final ApiService apiService;

    @Operation(
            summary = "Получить объекты с внешнего API",
            description = "Выполняет GET запрос на https://api.restful-api.dev/objects и логирует полученный ответ"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно получены"),
            @ApiResponse(responseCode = "500", description = "Ошибка при обращении к внешнему API"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/objects")
    public ResponseEntity<String> getObjectsFromExternalApi() {
        log.info("Получен запрос на получение объектов с внешнего API");

        try {
            String response = apiService.fetchObjectsFromExternalApi();

            log.info("Запрос к внешнему API выполнен успешно");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при обработке запроса к внешнему API: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Ошибка при получении данных с внешнего API: " + e.getMessage());
        }
    }
}
