package ru.alfabank.ufr.onespace.csv.parser.batch.config;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableAsync
public class CommonConfig {
    @Bean
    public JobLauncher commonJobLauncher(JobRepository jobRepository,
          @Qualifier("fileSeparatorTaskExecutor")TaskExecutor taskExecutor)
          throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public ConcurrentHashMap<String,Object> getCache(){
        return new ConcurrentHashMap<>();
    }

}
