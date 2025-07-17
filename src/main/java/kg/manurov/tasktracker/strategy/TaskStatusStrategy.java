package kg.manurov.tasktracker.strategy;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;

import java.util.Set;

public interface TaskStatusStrategy {

    TaskStatus getStatus();

    /**
     * Возвращает множество статусов, на которые можно перейти из текущего
     */
    Set<TaskStatus> getAllowedTransitions();

    boolean canTransitionTo(TaskStatus newStatus);

    void executeTransition(Task task, TaskStatus newStatus);

    String getStatusDescription();

    String getTransitionDescription();

    default boolean isFinal() {
        return getAllowedTransitions().isEmpty();
    }

    default void onEnter(Task task) {
        // По умолчанию ничего не делаем
    }

    default void onExit(Task task) {
        // По умолчанию ничего не делаем
    }
}
