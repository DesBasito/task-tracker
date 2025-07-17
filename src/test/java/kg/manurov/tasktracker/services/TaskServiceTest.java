package kg.manurov.tasktracker.services;

import kg.manurov.tasktracker.domain.dto.TaskDto;
import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.exception.TaskNotFoundException;
import kg.manurov.tasktracker.repositories.TaskRepository;
import kg.manurov.tasktracker.service.TaskService;
import kg.manurov.tasktracker.service.TaskStatusManager;
import kg.manurov.tasktracker.strategy.TaskStatusStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskStatusManager statusManager;

    @Mock
    private TaskStatusStrategy statusStrategy;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskDto testTaskRequest;
    private final Long TEST_ID = 1L;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(TEST_ID);
        testTask.setTitle("Тестовая задача");
        testTask.setDescription("Описание тестовой задачи");
        testTask.setStatus(TaskStatus.PENDING.name());
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        testTaskRequest = new TaskDto();
        testTaskRequest.setId(TEST_ID);
        testTaskRequest.setTitle("Тестовая задача");
        testTaskRequest.setDescription("Описание тестовой задачи");
        testTaskRequest.setStatus(TaskStatus.PENDING.name());
        testTaskRequest.setCreatedAt(LocalDateTime.now());
        testTaskRequest.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createTask_Success() {
        TaskDto newTaskRequest = new TaskDto();
        newTaskRequest.setTitle("Новая задача");
        newTaskRequest.setDescription("Описание новой задачи");

        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(statusManager.getStrategy(TaskStatus.PENDING)).thenReturn(statusStrategy);
        doNothing().when(statusStrategy).onEnter(any(Task.class));

        TaskDto result = taskService.createTask(newTaskRequest);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals("Тестовая задача", result.getTitle());
        assertEquals("Описание тестовой задачи", result.getDescription());
        assertEquals(TaskStatus.PENDING.name(), result.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(statusManager, times(1)).getStrategy(TaskStatus.PENDING);
        verify(statusStrategy, times(1)).onEnter(any(Task.class));
    }

    @Test
    void createTask_WithNullTitle_SavesWithoutValidation() {
        TaskDto invalidTaskRequest = new TaskDto();
        invalidTaskRequest.setTitle(null);
        invalidTaskRequest.setDescription("Описание");

        Task savedTask = new Task();
        savedTask.setId(TEST_ID);
        savedTask.setTitle(null);
        savedTask.setDescription("Описание");
        savedTask.setStatus(TaskStatus.PENDING.name());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(statusManager.getStrategy(TaskStatus.PENDING)).thenReturn(statusStrategy);
        doNothing().when(statusStrategy).onEnter(any(Task.class));

        TaskDto result = taskService.createTask(invalidTaskRequest);

        assertNotNull(result);
        assertNull(result.getTitle());
        assertEquals("Описание", result.getDescription());
        assertEquals(TaskStatus.PENDING.name(), result.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(statusManager, times(1)).getStrategy(TaskStatus.PENDING);
        verify(statusStrategy, times(1)).onEnter(any(Task.class));
    }

    @Test
    void createTask_WithNullDescription_SavesSuccessfully() {
        TaskDto taskRequest = new TaskDto();
        taskRequest.setTitle("Задача без описания");
        taskRequest.setDescription(null);

        Task savedTask = new Task();
        savedTask.setId(TEST_ID);
        savedTask.setTitle("Задача без описания");
        savedTask.setDescription(null);
        savedTask.setStatus(TaskStatus.PENDING.name());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(statusManager.getStrategy(TaskStatus.PENDING)).thenReturn(statusStrategy);
        doNothing().when(statusStrategy).onEnter(any(Task.class));

        TaskDto result = taskService.createTask(taskRequest);

        assertNotNull(result);
        assertEquals("Задача без описания", result.getTitle());
        assertNull(result.getDescription());
        assertEquals(TaskStatus.PENDING.name(), result.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WithEmptyFields_SavesSuccessfully() {
        TaskDto taskRequest = new TaskDto();
        taskRequest.setTitle("");
        taskRequest.setDescription("");

        Task savedTask = new Task();
        savedTask.setId(TEST_ID);
        savedTask.setTitle("");
        savedTask.setDescription("");
        savedTask.setStatus(TaskStatus.PENDING.name());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(statusManager.getStrategy(TaskStatus.PENDING)).thenReturn(statusStrategy);
        doNothing().when(statusStrategy).onEnter(any(Task.class));

        TaskDto result = taskService.createTask(taskRequest);

        assertNotNull(result);
        assertEquals("", result.getTitle());
        assertEquals("", result.getDescription());
        assertEquals(TaskStatus.PENDING.name(), result.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getAllTasks_Success() {
        List<Task> mockTasks = Arrays.asList(
                createMockTask(1L, "Задача 1", TaskStatus.PENDING),
                createMockTask(2L, "Задача 2", TaskStatus.IN_PROGRESS),
                createMockTask(3L, "Задача 3", TaskStatus.COMPLETED)
        );

        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mockTasks);

        List<TaskDto> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Задача 1", result.get(0).getTitle());
        assertEquals("Задача 2", result.get(1).getTitle());
        assertEquals("Задача 3", result.get(2).getTitle());

        verify(taskRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllTasks_EmptyList() {
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList());

        List<TaskDto> result = taskService.getAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getTaskById_Success() {
        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));

        TaskDto result = taskService.getTaskById(TEST_ID);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals("Тестовая задача", result.getTitle());
        assertEquals("Описание тестовой задачи", result.getDescription());
        assertEquals(TaskStatus.PENDING.name(), result.getStatus());

        verify(taskRepository, times(1)).findById(TEST_ID);
    }

    @Test
    void getTaskById_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(999L);
        });
    }

    @Test
    void updateTask_Success() {
        TaskDto updateRequest = new TaskDto();
        updateRequest.setTitle("Обновленная задача");
        updateRequest.setDescription("Обновленное описание");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS.getDescription());

        Task updatedTask = new Task();
        updatedTask.setId(TEST_ID);
        updatedTask.setTitle("Обновленная задача");
        updatedTask.setDescription("Обновленное описание");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS.name());

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        doNothing().when(statusManager).executeTransition(any(Task.class), any(TaskStatus.class));

        TaskDto result = taskService.updateTask(TEST_ID, updateRequest);

        assertNotNull(result);
        assertEquals("Обновленная задача", result.getTitle());
        assertEquals("Обновленное описание", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS.name(), result.getStatus());

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(statusManager, times(1)).executeTransition(any(Task.class), eq(TaskStatus.IN_PROGRESS));
    }

    @Test
    void updateTask_OnlyTitle_Success() {
        TaskDto updateRequest = new TaskDto();
        updateRequest.setTitle("Только новый заголовок");

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDto result = taskService.updateTask(TEST_ID, updateRequest);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(statusManager, never()).executeTransition(any(Task.class), any(TaskStatus.class));
    }

    @Test
    void updateTask_InvalidStatus_ThrowsException() {
        TaskDto updateRequest = new TaskDto();
        updateRequest.setStatus("INVALID_STATUS");

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(TEST_ID, updateRequest);
        });

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_NotFound_ThrowsException() {
        TaskDto updateRequest = new TaskDto();
        updateRequest.setTitle("Обновленная задача");

        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(999L, updateRequest);
        });

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void changeTaskStatus_Success() {
        TaskStatus newStatus = TaskStatus.IN_PROGRESS;
        Task updatedTask = new Task();
        updatedTask.setId(TEST_ID);
        updatedTask.setStatus(newStatus.name());

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        doNothing().when(statusManager).executeTransition(any(Task.class), eq(newStatus));

        TaskDto result = taskService.changeTaskStatus(TEST_ID, newStatus);

        assertNotNull(result);
        assertEquals(newStatus.name(), result.getStatus());

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(statusManager, times(1)).executeTransition(any(Task.class), eq(newStatus));
    }

    @Test
    void changeTaskStatus_SameStatus_NoChange() {
        TaskStatus currentStatus = TaskStatus.PENDING;

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));

        TaskDto result = taskService.changeTaskStatus(TEST_ID, currentStatus);

        assertNotNull(result);
        assertEquals(currentStatus.name(), result.getStatus());

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, never()).save(any(Task.class));
        verify(statusManager, never()).executeTransition(any(Task.class), any(TaskStatus.class));
    }

    @Test
    void changeTaskStatus_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.changeTaskStatus(999L, TaskStatus.IN_PROGRESS);
        });

        verify(taskRepository, times(1)).findById(999L);
        verify(statusManager, never()).executeTransition(any(Task.class), any(TaskStatus.class));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));
        when(statusManager.isFinalStatus(TaskStatus.PENDING)).thenReturn(false);
        doNothing().when(taskRepository).delete(testTask);

        taskService.deleteTask(TEST_ID);

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, times(1)).delete(testTask);
        verify(statusManager, times(1)).isFinalStatus(TaskStatus.PENDING);
    }

    @Test
    void deleteTask_FinalStatus_Success() {
        Task completedTask = new Task();
        completedTask.setId(TEST_ID);
        completedTask.setStatus(TaskStatus.COMPLETED.name());

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(completedTask));
        when(statusManager.isFinalStatus(TaskStatus.COMPLETED)).thenReturn(true);
        doNothing().when(taskRepository).delete(completedTask);

        taskService.deleteTask(TEST_ID);

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(taskRepository, times(1)).delete(completedTask);
        verify(statusManager, times(1)).isFinalStatus(TaskStatus.COMPLETED);
    }

    @Test
    void deleteTask_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void getTasksByStatus_Success() {
        TaskStatus status = TaskStatus.IN_PROGRESS;
        List<Task> mockTasks = Arrays.asList(
                createMockTask(1L, "Задача 1", status),
                createMockTask(2L, "Задача 2", status)
        );

        when(taskRepository.findByStatus(status.name())).thenReturn(mockTasks);

        List<TaskDto> result = taskService.getTasksByStatus(status);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(status.name(), result.get(0).getStatus());
        assertEquals(status.name(), result.get(1).getStatus());

        verify(taskRepository, times(1)).findByStatus(status.name());
    }

    @Test
    void getTasksByStatus_EmptyList() {
        TaskStatus status = TaskStatus.CANCELLED;
        when(taskRepository.findByStatus(status.name())).thenReturn(Arrays.asList());

        List<TaskDto> result = taskService.getTasksByStatus(status);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findByStatus(status.name());
    }

    @Test
    void getAvailableTransitions_Success() {
        List<TaskStatus> availableTransitions = Arrays.asList(
                TaskStatus.IN_PROGRESS,
                TaskStatus.CANCELLED
        );

        when(taskRepository.findById(TEST_ID)).thenReturn(Optional.of(testTask));
        when(statusManager.getAvailableTransitions(TaskStatus.PENDING)).thenReturn(availableTransitions);

        List<TaskStatus> result = taskService.getAvailableTransitions(TEST_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(TaskStatus.IN_PROGRESS));
        assertTrue(result.contains(TaskStatus.CANCELLED));

        verify(taskRepository, times(1)).findById(TEST_ID);
        verify(statusManager, times(1)).getAvailableTransitions(TaskStatus.PENDING);
    }

    @Test
    void getAvailableTransitions_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getAvailableTransitions(999L);
        });

        verify(taskRepository, times(1)).findById(999L);
        verify(statusManager, never()).getAvailableTransitions(any(TaskStatus.class));
    }

    private Task createMockTask(Long id, String title, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Описание для " + title);
        task.setStatus(status.name());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }
}