package ru.sfedu.mmcs_nexus.model.payload.admin;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.validators.EventName;
import ru.sfedu.mmcs_nexus.validators.EventYear;
import ru.sfedu.mmcs_nexus.validators.MaxPoints;

//todo add validations
@Getter
@Setter
public class CreateProjectRequestPayload {
    private Integer externalId;
    private Integer quantityOfStudents;
    private String captainName;
    private boolean isFull;
    private String track;
    private String technologies;

    private String name;
    private String description;
    private String type;
    private int year;
}
