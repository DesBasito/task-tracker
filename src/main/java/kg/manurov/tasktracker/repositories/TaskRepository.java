package kg.manurov.tasktracker.repositories;

import kg.manurov.tasktracker.domain.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(String status);

    List<Task> findAllByOrderByCreatedAtDesc();
}
