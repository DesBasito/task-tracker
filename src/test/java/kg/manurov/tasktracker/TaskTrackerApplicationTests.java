package kg.manurov.tasktracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskTrackerApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Контекст приложения должен загружаться успешно")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Основные компоненты должны быть доступны")
    void mainComponentsShouldBeAvailable() {
        assertThat(applicationContext.containsBean("taskRepository")).isTrue();
        assertThat(applicationContext.containsBean("userRepository")).isTrue();
        assertThat(applicationContext.containsBean("taskService")).isTrue();
        assertThat(applicationContext.containsBean("taskController")).isTrue();
        assertThat(applicationContext.containsBean("securityFilterChain")).isTrue();
        assertThat(applicationContext.containsBean("authenticationProvider")).isTrue();
        assertThat(applicationContext.containsBean("authenticationManager")).isTrue();
        assertThat(applicationContext.containsBean("errorService")).isTrue();
        assertThat(applicationContext.containsBean("userDetailsService")).isTrue();
        assertThat(applicationContext.containsBean("passwordEncoder")).isTrue();
        assertThat(applicationContext.containsBean("cacheConfiguration")).isTrue();
        assertThat(applicationContext.containsBean("redisCacheManagerBuilderCustomizer")).isTrue();
        assertThat(applicationContext.containsBean("customOpenAPI")).isTrue();
    }

}
