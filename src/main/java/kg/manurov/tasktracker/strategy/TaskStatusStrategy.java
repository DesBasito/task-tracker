package kg.manurov.tasktracker.strategy;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;

import java.util.Set;

public interface TaskStatusStrategy {

    TaskStatus getStatus();

    Set<TaskStatus> getAllowedTransitions();

    boolean canTransitionTo(TaskStatus newStatus);

    void executeTransition(Task task, TaskStatus newStatus);

    String getStatusDescription();

    String getTransitionDescription();

    default boolean isFinal() {
        return getAllowedTransitions().isEmpty();
    }

    default void onEnter(Task task) {}

    default void onExit(Task task) {}
}
