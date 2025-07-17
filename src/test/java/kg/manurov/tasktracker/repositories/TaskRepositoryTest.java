package kg.manurov.tasktracker.repositories;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Task testTask1;
    private Task testTask2;
    private Task testTask3;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Создаем тестовые задачи
        testTask1 = createTask("Первая задача", "Описание первой задачи", TaskStatus.PENDING);
        testTask2 = createTask("Вторая задача", "Описание второй задачи", TaskStatus.IN_PROGRESS);
        testTask3 = createTask("Третья задача", "Описание третьей задачи", TaskStatus.COMPLETED);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void findById_ExistingTask_ReturnsTask() {

        Task savedTask = entityManager.persistAndFlush(testTask1);

        Optional<Task> result = taskRepository.findById(savedTask.getId());

        assertTrue(result.isPresent());
        Task foundTask = result.get();
        assertEquals(savedTask.getId(), foundTask.getId());
        assertEquals("Первая задача", foundTask.getTitle());
        assertEquals("Описание первой задачи", foundTask.getDescription());
        assertEquals(TaskStatus.PENDING.name(), foundTask.getStatus());
        assertNotNull(foundTask.getCreatedAt());
        assertNotNull(foundTask.getUpdatedAt());
    }

    @Test
    void findById_NonExistingTask_ReturnsEmpty() {
        Optional<Task> result = taskRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void save_NewTask_PersistsCorrectly() {

        Task newTask = createTask("Новая задача", "Описание новой задачи", TaskStatus.PENDING);

        Task savedTask = taskRepository.save(newTask);

        assertNotNull(savedTask.getId());
        assertEquals("Новая задача", savedTask.getTitle());
        assertEquals("Описание новой задачи", savedTask.getDescription());
        assertEquals(TaskStatus.PENDING.name(), savedTask.getStatus());
        assertNotNull(savedTask.getCreatedAt());
        assertNotNull(savedTask.getUpdatedAt());

        // Проверяем, что задача действительно сохранена в базе
        Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());
        assertTrue(retrievedTask.isPresent());
        assertEquals(savedTask.getTitle(), retrievedTask.get().getTitle());
    }

    @Test
    void save_UpdateExistingTask_UpdatesCorrectly() {

        Task savedTask = entityManager.persistAndFlush(testTask1);
        LocalDateTime originalUpdatedAt = savedTask.getUpdatedAt();

        // Добавляем небольшую задержку для обновления времени
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        savedTask.setTitle("Обновленный заголовок");
        savedTask.setDescription("Обновленное описание");
        savedTask.setStatus(TaskStatus.IN_PROGRESS.name());
        savedTask.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(savedTask);

        assertEquals(savedTask.getId(), updatedTask.getId());
        assertEquals("Обновленный заголовок", updatedTask.getTitle());
        assertEquals("Обновленное описание", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS.name(), updatedTask.getStatus());
        assertTrue(updatedTask.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void delete_ExistingTask_RemovesFromDatabase() {

        Task savedTask = entityManager.persistAndFlush(testTask1);
        Long taskId = savedTask.getId();

        taskRepository.delete(savedTask);
        entityManager.flush();

        Optional<Task> deletedTask = taskRepository.findById(taskId);
        assertFalse(deletedTask.isPresent());
    }

    @Test
    void findAll_MultiipleTasks_ReturnsAllTasks() {

        entityManager.persistAndFlush(testTask1);
        entityManager.persistAndFlush(testTask2);
        entityManager.persistAndFlush(testTask3);

        List<Task> allTasks = taskRepository.findAll();

        assertEquals(3, allTasks.size());

        List<String> titles = allTasks.stream()
                .map(Task::getTitle)
                .toList();
        assertTrue(titles.contains("Первая задача"));
        assertTrue(titles.contains("Вторая задача"));
        assertTrue(titles.contains("Третья задача"));
    }

    @Test
    void findAllByOrderByCreatedAtDesc_MultiipleTasks_ReturnsTasksInDescendingOrder() {
        Task firstTask = entityManager.persistAndFlush(testTask1);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Task secondTask = entityManager.persistAndFlush(testTask2);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Task thirdTask = entityManager.persistAndFlush(testTask3);

        List<Task> orderedTasks = taskRepository.findAllByOrderByCreatedAtDesc();

        assertEquals(3, orderedTasks.size());

        assertTrue(orderedTasks.get(0).getCreatedAt().isAfter(orderedTasks.get(1).getCreatedAt()) ||
                   orderedTasks.get(0).getCreatedAt().equals(orderedTasks.get(1).getCreatedAt()));
        assertTrue(orderedTasks.get(1).getCreatedAt().isAfter(orderedTasks.get(2).getCreatedAt()) ||
                   orderedTasks.get(1).getCreatedAt().equals(orderedTasks.get(2).getCreatedAt()));
    }

    @Test
    void findByStatus_ExistingStatus_ReturnsMatchingTasks() {
        entityManager.persistAndFlush(testTask1); // PENDING
        entityManager.persistAndFlush(testTask2); // IN_PROGRESS
        entityManager.persistAndFlush(testTask3); // COMPLETED

        Task anotherPendingTask = createTask("Четвертая задача", "Описание четвертой задачи", TaskStatus.PENDING);
        entityManager.persistAndFlush(anotherPendingTask);

        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING.name());
        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS.name());
        List<Task> completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETED.name());

        assertEquals(2, pendingTasks.size());
        assertEquals(1, inProgressTasks.size());
        assertEquals(1, completedTasks.size());

        pendingTasks.forEach(task -> assertEquals(TaskStatus.PENDING.name(), task.getStatus()));
        inProgressTasks.forEach(task -> assertEquals(TaskStatus.IN_PROGRESS.name(), task.getStatus()));
        completedTasks.forEach(task -> assertEquals(TaskStatus.COMPLETED.name(), task.getStatus()));
    }

    @Test
    void findByStatus_NonExistingStatus_ReturnsEmptyList() {

        entityManager.persistAndFlush(testTask1);
        entityManager.persistAndFlush(testTask2);

        List<Task> cancelledTasks = taskRepository.findByStatus(TaskStatus.CANCELLED.name());

        assertTrue(cancelledTasks.isEmpty());
    }

    @Test
    void findByStatus_EmptyDatabase_ReturnsEmptyList() {
        List<Task> tasks = taskRepository.findByStatus(TaskStatus.PENDING.name());

        assertTrue(tasks.isEmpty());
    }

    @Test
    void count_MultipleTasks_ReturnsCorrectCount() {

        entityManager.persistAndFlush(testTask1);
        entityManager.persistAndFlush(testTask2);
        entityManager.persistAndFlush(testTask3);

        long count = taskRepository.count();

        assertEquals(3, count);
    }

    @Test
    void count_EmptyDatabase_ReturnsZero() {
        long count = taskRepository.count();

        assertEquals(0, count);
    }

    @Test
    void deleteAll_MultipleTasks_RemovesAllTasks() {

        entityManager.persistAndFlush(testTask1);
        entityManager.persistAndFlush(testTask2);
        entityManager.persistAndFlush(testTask3);

        // Проверяем, что задачи существуют
        assertEquals(3, taskRepository.count());

        taskRepository.deleteAll();
        entityManager.flush();

        assertEquals(0, taskRepository.count());
        assertTrue(taskRepository.findAll().isEmpty());
    }

    @Test
    void existsById_ExistingTask_ReturnsTrue() {

        Task savedTask = entityManager.persistAndFlush(testTask1);

        boolean exists = taskRepository.existsById(savedTask.getId());

        assertTrue(exists);
    }

    @Test
    void existsById_NonExistingTask_ReturnsFalse() {
        boolean exists = taskRepository.existsById(999L);

        assertFalse(exists);
    }

    @Test
    void save_TaskWithNullFields_HandlesGracefully() {

        Task taskWithNullFields = new Task();
        taskWithNullFields.setTitle("Задача с null полями");
        taskWithNullFields.setStatus(TaskStatus.PENDING.name());
        taskWithNullFields.setCreatedAt(LocalDateTime.now());
        taskWithNullFields.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(taskWithNullFields);

        assertNotNull(savedTask.getId());
        assertEquals("Задача с null полями", savedTask.getTitle());
        assertNull(savedTask.getDescription());
        assertEquals(TaskStatus.PENDING.name(), savedTask.getStatus());
    }

    @Test
    void findAllByOrderByCreatedAtDesc_EmptyDatabase_ReturnsEmptyList() {
        List<Task> tasks = taskRepository.findAllByOrderByCreatedAtDesc();

        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void save_TaskWithLongDescription_PersistsCorrectly() {

        String longDescription = "Очень длинное описание задачи ".repeat(50);
        Task taskWithLongDescription = createTask("Задача с длинным описанием", longDescription, TaskStatus.PENDING);

        Task savedTask = taskRepository.save(taskWithLongDescription);

        assertNotNull(savedTask.getId());
        assertEquals("Задача с длинным описанием", savedTask.getTitle());
        assertEquals(longDescription, savedTask.getDescription());
        assertEquals(TaskStatus.PENDING.name(), savedTask.getStatus());

        Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());
        assertTrue(retrievedTask.isPresent());
        assertEquals(longDescription, retrievedTask.get().getDescription());
    }

    private Task createTask(String title, String description, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status.name());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }
}