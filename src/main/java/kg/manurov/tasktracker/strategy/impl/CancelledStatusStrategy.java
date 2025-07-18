package kg.manurov.tasktracker.strategy.impl;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.strategy.TaskStatusStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class CancelledStatusStrategy implements TaskStatusStrategy {

    @Override
    public TaskStatus getStatus() {
        return TaskStatus.CANCELLED;
    }

    @Override
    public Set<TaskStatus> getAllowedTransitions() {
        return Set.of();
    }

    @Override
    public boolean canTransitionTo(TaskStatus newStatus) {
        return false;
    }

    @Override
    public void executeTransition(Task task, TaskStatus newStatus) {
        throw new IllegalStateException(
                "Отмененную задачу нельзя изменить. Задача находится в финальном состоянии."
        );
    }

    @Override
    public String getStatusDescription() {
        return "Отменена";
    }

    @Override
    public String getTransitionDescription() {
        return "Статус 'Отменена' является финальным. Переходы невозможны.";
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public void onEnter(Task task) {
        log.info("Задача {} отменена и зафиксирована", task.getId());
    }

    @Override
    public void onExit(Task task) {
        log.warn("Попытка выхода из финального статуса CANCELLED для задачи {}", task.getId());
    }
}