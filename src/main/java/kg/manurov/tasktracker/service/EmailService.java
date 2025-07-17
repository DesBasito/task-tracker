package kg.manurov.tasktracker.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kg.manurov.tasktracker.domain.dto.TaskDto;
import kg.manurov.tasktracker.domain.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    public static final String TD_STYLE_PADDING_8_PX = "<td style='padding: 8px;'>";
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String EMAIL_FROM;

    public void sendTasksReport(String to, List<TaskDto> tasks) throws MessagingException, UnsupportedEncodingException {
        log.info("Отправка {} задач на email: {}", tasks.size(), to);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

        helper.setFrom(EMAIL_FROM, "Task Tracker");
        helper.setTo(to);
        helper.setSubject("Все задачи из системы");

        String emailContent = generateSimpleTasksList(tasks);
        helper.setText(emailContent, true);

        mailSender.send(message);
        log.info("Задачи успешно отправлены на {}", to);
    }

    private String generateSimpleTasksList(List<TaskDto> tasks) {
        StringBuilder html = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        html.append("<html><body>")
                .append("<h2>Все задачи из системы</h2>")
                .append("<p>Всего задач: ").append(tasks.size()).append("</p>");

        if (tasks.isEmpty()) {
            html.append("<p>Задач пока нет</p>");
        } else {
            html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
                    .append("<tr style='background-color: #f2f2f2;'>")
                    .append("<th style='padding: 8px;'>№</th>")
                    .append("<th style='padding: 8px;'>Название</th>")
                    .append("<th style='padding: 8px;'>Описание</th>")
                    .append("<th style='padding: 8px;'>Статус</th>")
                    .append("<th style='padding: 8px;'>Создано</th>")
                    .append("</tr>");

            for (int i = 0; i < tasks.size(); i++) {
                TaskDto task = tasks.get(i);
                html.append("<tr>")
                        .append(TD_STYLE_PADDING_8_PX).append(i + 1).append("</td>")
                        .append(TD_STYLE_PADDING_8_PX).append(task.getTitle()).append("</td>")
                        .append(TD_STYLE_PADDING_8_PX).append(task.getDescription() != null ? task.getDescription() : "-").append("</td>")
                        .append(TD_STYLE_PADDING_8_PX).append(TaskStatus.valueOf(task.getStatus()).getDescription()).append("</td>")
                        .append(TD_STYLE_PADDING_8_PX).append(task.getCreatedAt() != null ? task.getCreatedAt().format(formatter) : "-").append("</td>")
                        .append("</tr>");
            }
            html.append("</table>");
        }

        html.append("</body></html>");
        return html.toString();
    }
}
