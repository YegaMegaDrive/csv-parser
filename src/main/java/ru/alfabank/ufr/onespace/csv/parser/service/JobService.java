package ru.alfabank.ufr.onespace.csv.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;
import ru.alfabank.ufr.onespace.csv.parser.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class JobService {

    private final ConcurrentHashMap<String, Object> cache;

    private final JobLauncher jobLauncher;

    private final Job job;

    @Autowired
    public JobService(ConcurrentHashMap<String, Object> cache,
          JobLauncher jobLauncher, @Qualifier("fileSeparatorJob") Job job) {
        this.cache = cache;
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    public void uploadFile(InputStream file,String name) throws IOException {
        cache.put("filename", name);

        FileSeparatorJobRequest params = new FileSeparatorJobRequest();
        params.afterPropertiesSet();
        cache.put("encoding", Utils.processFileEncoding(file));
        cache.put("params", params);
        try {
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addDate("date", new Date());
            jobLauncher.run(job, builder.toJobParameters());
        } catch (Exception e) {
            log.error("Error while running job:{}", e.getMessage());
        }
    }
}
