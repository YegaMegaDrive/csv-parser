package ru.alfabank.ufr.onespace.csv.parser.batch.listeners;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;
import ru.alfabank.ufr.onespace.csv.parser.utils.Utils;

import java.io.IOException;

@Component
@Slf4j
@StepScope
public class GridMongoStepListener implements StepExecutionListener {
    private final ExecutionContext executionContext;

    private final GridFsOperations gridFsTemplate;

    @Autowired
    public GridMongoStepListener(
          @Qualifier("gridMongoExecutionContext") ExecutionContext executionContext,
          GridFsTemplate gridFsTemplate) {
        this.executionContext = executionContext;
        this.gridFsTemplate = gridFsTemplate;
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
        FileSeparatorJobRequest params =
              (FileSeparatorJobRequest) executionContext.get("params");
        GridFSFile file =
              gridFsTemplate.findOne(new Query(Criteria.where("_id").is(params.getFileField())));
        executionContext.putString("encoding", file.getMetadata().get("encoding",
              Utils.STANDARD_ENCODING));
        try {
            executionContext.put("fileBytes",gridFsTemplate.getResource(file).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("StepExecution execution context map : {}", executionContext);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        executionContext.remove("fileId");
        executionContext.remove("fileBytes");
        executionContext.remove("encoding");
        log.info("StepExecution execution context map : {}", executionContext);
        return ExitStatus.COMPLETED;
    }
}
