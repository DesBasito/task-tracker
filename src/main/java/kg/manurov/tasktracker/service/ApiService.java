package kg.manurov.tasktracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiService {
    private final RestTemplate restTemplate;
    @Value("${external.api.url:https://api.restful-api.dev/objects}")
    private String externalApiUrl;

    public String fetchObjectsFromExternalApi() {
        log.info("Выполняется GET запрос на: {}", externalApiUrl);

        try {
            String response = restTemplate.getForObject(externalApiUrl, String.class);

            log.info("Получен ответ от внешнего API:");
            log.info("URL: {}", externalApiUrl);
            log.info("Размер ответа: {} символов", response != null ? response.length() : 0);
            log.info("Содержимое ответа: {}", response);

            return response;

        } catch (RestClientException e) {
            log.error("Ошибка при выполнении запроса к внешнему API: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось получить данные с внешнего API", e);
        }
    }
}
