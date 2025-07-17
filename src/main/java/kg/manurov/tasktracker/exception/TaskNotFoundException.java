package kg.manurov.tasktracker.exception;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
public class TaskNotFoundException extends RuntimeException {
    private final String msg;
}
