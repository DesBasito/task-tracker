package kg.manurov.tasktracker.service;

import kg.manurov.tasktracker.handler.ErrorResponseBody;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ErrorService {
    public ErrorResponseBody makeResponse(BindingResult bindingResult) {
        Map<String, List<String>> reasons = new HashMap<>();
        bindingResult.getFieldErrors().stream()
                .filter(error -> error.getDefaultMessage() != null)
                .forEach(err -> {
                    if (!reasons.containsKey(err.getField())) {
                        reasons.put(err.getField(), new ArrayList<>());
                    }
                    reasons.get(err.getField()).add(err.getDefaultMessage());
                });
        return new ErrorResponseBody("Ошибка валидации", reasons);
    }
}
