package ru.alfabank.ufr.onespace.csv.parser.controller;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;
import ru.alfabank.ufr.onespace.csv.parser.service.JobService;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@RestController
public class TestController {

    private final JobService jobService;

    private ExecutionContext context;

    private Job mongoGridJob;

    private JobLauncher jobLauncher;

    public TestController(JobService jobService,
          @Qualifier("gridMongoExecutionContext") ExecutionContext context,
          @Qualifier("gridMongoJob") Job mongoGridJob,
          JobLauncher jobLauncher) {
        this.jobService = jobService;
        this.context = context;
        this.mongoGridJob = mongoGridJob;
        this.jobLauncher = jobLauncher;

    }

    @GetMapping("/upload")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void uploadFile() throws IOException {
        //TODO For test, remove later
        String filename = "not";

        File file = new File(filename + ".csv");
        jobService.uploadFile(FileUtils.openInputStream(file), filename);
    }

    @GetMapping("/test")
    public void test() throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException, JobParametersInvalidException,
          JobRestartException {
        FileSeparatorJobRequest jobParams = new FileSeparatorJobRequest();
        jobParams.afterPropertiesSet();
        jobParams.setFileField(new ObjectId("63ff00ee9bd9d743af714a4c"));
        context.put("params", jobParams);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addDate("date", new Date());
        jobLauncher.run(mongoGridJob, builder.toJobParameters());
    }

}
