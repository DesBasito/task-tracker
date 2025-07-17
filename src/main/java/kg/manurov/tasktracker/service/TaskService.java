package kg.manurov.tasktracker.service;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.dto.TaskRequest;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.exception.TaskNotFoundException;
import kg.manurov.tasktracker.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskStatusManager statusManager;

    public TaskRequest createTask(TaskRequest taskDTO) {
        log.info("Создание новой задачи: {}", taskDTO.getTitle());

        Task task = convertToEntity(taskDTO);

        task.setStatus(TaskStatus.PENDING.name());

        Task savedTask = taskRepository.save(task);

        statusManager.getStrategy(TaskStatus.PENDING).onEnter(savedTask);

        log.info("Задача успешно создана с ID: {}", savedTask.getId());
        return convertToDTO(savedTask);
    }


    @Cacheable(value = "tasksCache", key = "'all_tasks'")
    public List<TaskRequest> getAllTasks() {
        log.info("Получение списка всех задач");

        List<Task> tasks = taskRepository.findAllByOrderByCreatedAtDesc();

        log.info("Найдено {} задач", tasks.size());
        return tasks.stream()
                .map(this::convertToDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public TaskRequest getTaskById(Long id) {
        log.info("Поиск задачи с ID: {}", id);

        Task task = findTaskById(id);

        log.info("Задача найдена: {}", task.getTitle());
        return convertToDTO(task);
    }


    public TaskRequest updateTask(Long id, TaskRequest taskDTO) {
        log.info("Обновление задачи с ID: {}", id);

        Task existingTask = findTaskById(id);
        TaskStatus oldStatus = TaskStatus.valueOf(existingTask.getStatus());

        if (taskDTO.getTitle() != null) {
            existingTask.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            existingTask.setDescription(taskDTO.getDescription());
        }

        if (taskDTO.getStatus() != null) {
            TaskStatus newStatus = TaskStatus.getType(taskDTO.getStatus())
                    .orElseThrow(() -> new IllegalArgumentException("Неверный статус: " + taskDTO.getStatus()));

            if (oldStatus != newStatus) {
                statusManager.executeTransition(existingTask, newStatus);
            }
        }

        Task updatedTask = taskRepository.save(existingTask);

        log.info("Задача с ID {} успешно обновлена", id);
        return convertToDTO(updatedTask);
    }


    public TaskRequest changeTaskStatus(Long id, TaskStatus newStatus) {
        log.info("Изменение статуса задачи {} на {}", id, newStatus);

        Task task = findTaskById(id);
        TaskStatus currentStatus = TaskStatus.valueOf(task.getStatus());

        if (currentStatus == newStatus) {
            log.info("Статус задачи {} уже установлен в {}", id, newStatus);
            return convertToDTO(task);
        }

        statusManager.executeTransition(task, newStatus);

        Task updatedTask = taskRepository.save(task);

        log.info("Статус задачи {} успешно изменен с {} на {}", id, currentStatus, newStatus);
        return convertToDTO(updatedTask);
    }

    public void deleteTask(Long id) {
        log.info("Удаление задачи с ID: {}", id);

        Task task = findTaskById(id);

        if (statusManager.isFinalStatus(TaskStatus.valueOf(task.getStatus()))) {
            log.info("Удаление финальной задачи {}", id);
        } else {
            log.warn("Удаление активной задачи {}, статус: {}", id, task.getStatus());
        }

        taskRepository.delete(task);
        log.info("Задача с ID {} успешно удалена", id);
    }

    @Transactional(readOnly = true)
    public List<TaskRequest> getTasksByStatus(TaskStatus status) {
        log.info("Поиск задач по статусу: {}", status);

        List<Task> tasks = taskRepository.findByStatus(status.name());

        log.info("Найдено {} задач со статусом {}", tasks.size(), status);
        return tasks.stream()
                .map(this::convertToDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<TaskStatus> getAvailableTransitions(Long taskId) {
        Task task = findTaskById(taskId);
        return statusManager.getAvailableTransitions(TaskStatus.valueOf(task.getStatus()));
    }



    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Задача с ID {} не найдена", id);
                    return new TaskNotFoundException("Задача с ID " + id + " не найдена");
                });
    }

    private TaskRequest convertToDTO(Task task) {
        TaskRequest dto = new TaskRequest();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    private Task convertToEntity(TaskRequest dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            TaskStatus status = TaskStatus.fromString(dto.getStatus())
                    .orElseThrow(() -> new IllegalArgumentException("Неверный статус: " + dto.getStatus()));
            task.setStatus(status.name());
        }

        return task;
    }
}