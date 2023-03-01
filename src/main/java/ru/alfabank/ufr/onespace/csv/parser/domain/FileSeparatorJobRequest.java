package ru.alfabank.ufr.onespace.csv.parser.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSeparatorJobRequest implements Serializable {

    private static final LocalDateTime TEST_DATES = LocalDateTime.now();
    private static final String TEST_NAME = "message";
    private static final NotificationStatus TEST_STATUS =
          NotificationStatus.INFORMED;
    private static final String TEST_TYPE = "001";

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime actionDate;

    private String type;

    private String name;

    private NotificationStatus status;

    @JsonIgnore
    private ObjectId fileField;

    public void afterPropertiesSet() {
        startDate = TEST_DATES;
        actionDate = TEST_DATES;
        endDate = TEST_DATES;
        status = TEST_STATUS;
        name = TEST_NAME;
        type = TEST_TYPE;
    }


}
