package kg.manurov.tasktracker.service;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.strategy.TaskStatusStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatusManager {

    private final List<TaskStatusStrategy> strategies;
    private Map<TaskStatus, TaskStatusStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        TaskStatusStrategy::getStatus,
                        Function.identity()
                ));

        log.info("Инициализирован TaskStatusManager с {} стратегиями", strategies.size());
        strategyMap.keySet().forEach(status ->
                log.debug("Зарегистрирована стратегия для статуса: {}", status)
        );
    }

    public TaskStatusStrategy getStrategy(TaskStatus status) {
        TaskStatusStrategy strategy = strategyMap.get(status);
        if (strategy == null) {
            throw new IllegalArgumentException("Стратегия для статуса " + status + " не найдена");
        }
        return strategy;
    }


    public boolean canTransition(TaskStatus fromStatus, TaskStatus toStatus) {
        TaskStatusStrategy strategy = getStrategy(fromStatus);
        return strategy.canTransitionTo(toStatus);
    }


    public void executeTransition(Task task, TaskStatus newStatus) {
        TaskStatus currentStatus = TaskStatus.valueOf(task.getStatus());

        log.info("Выполнение перехода задачи {} с {} на {}",
                task.getId(), currentStatus, newStatus);

        TaskStatusStrategy currentStrategy = getStrategy(currentStatus);

        currentStrategy.executeTransition(task, newStatus);

        TaskStatusStrategy newStrategy = getStrategy(newStatus);
        newStrategy.onEnter(task);

        log.info("Переход задачи {} завершен успешно", task.getId());
    }


    public String getTransitionDescription(TaskStatus status) {
        return getStrategy(status).getTransitionDescription();
    }

    public List<TaskStatus> getAvailableTransitions(TaskStatus status) {
        return getStrategy(status).getAllowedTransitions()
                .stream()
                .toList();
    }

    public boolean isFinalStatus(TaskStatus status) {
        return getStrategy(status).isFinal();
    }


    public void validateTransition(TaskStatus fromStatus, TaskStatus toStatus) {
        if (!canTransition(fromStatus, toStatus)) {
            TaskStatusStrategy strategy = getStrategy(fromStatus);
            throw new IllegalStateException(
                    String.format("Невозможно изменить статус с '%s' на '%s'. %s",
                            strategy.getStatusDescription(),
                            getStrategy(toStatus).getStatusDescription(),
                            strategy.getTransitionDescription())
            );
        }
    }
}