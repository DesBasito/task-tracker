package kg.manurov.tasktracker.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum TaskStatus {
    PENDING("В ожидании"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String description;

    public static boolean exists(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return Arrays.stream(TaskStatus.values())
                .anyMatch(status -> status.name().equalsIgnoreCase(value.strip()));
    }

    public static Optional<TaskStatus> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(TaskStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(value.strip()))
                .findFirst();
    }

    public static Optional<TaskStatus> getType(String value) {
        for (TaskStatus type : TaskStatus.values()) {
            if (type.getDescription().equalsIgnoreCase(value.strip())) {
                return Optional.of(type);
            }
        }
        throw new IllegalArgumentException(String.format("Тип %s не найден", value));
    }

    public static String getAvailableStatusesDescription() {
        String statuses = Arrays.stream(TaskStatus.values())
                .map(TaskStatus::name)
                .collect(Collectors.joining(", "));

        return "Указан неверный статус. Доступные статусы: " + statuses;
    }


    public static String getAllStatusDescriptions() {
        return Arrays.stream(TaskStatus.values())
                .map(TaskStatus::getDescription)
                .collect(Collectors.joining(", "));
    }


    @Override
    public String toString() {
        return name();
    }
}