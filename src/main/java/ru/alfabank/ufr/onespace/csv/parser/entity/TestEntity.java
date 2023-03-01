package ru.alfabank.ufr.onespace.csv.parser.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.alfabank.ufr.onespace.csv.parser.domain.NotificationStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@Data
@Builder
@Document(collection = "notifications")
public class TestEntity {

    @Id
    private String id;

    /**
     * pinEq Идентификатор клиента символьный
     */
    @NotNull
    @Indexed
    private String pinEq;

    /**
     * Дата, с которой начать уведомление клиента
     */
    private LocalDateTime startDate;

    /**
     * Дата, после которой уведомление не актуально
     */
    private LocalDateTime endDate;

    /**
     * Дата уведомления клиента
     */
    private LocalDateTime actionDate;

    /**
     * Тип уведомления символьный
     */
    private String type;

    /**
     * Тема уведомления
     */
    private String name;

    /**
     * Текст, который нужно вывести клиенту
     */
    private String text;

    /**
     * Статус уведомления
     */
    private NotificationStatus status;

    /**
     * Идентификатор файла с реестром уведомлений
     */
    private ObjectId fileId;
}
