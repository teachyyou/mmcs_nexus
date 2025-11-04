package ru.sfedu.mmcs_nexus.model.payload.admin;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.validators.EventName;
import ru.sfedu.mmcs_nexus.validators.EventYear;
import ru.sfedu.mmcs_nexus.validators.MaxPoints;

@Setter
@Getter
public class CreateEventRequestPayload {
    @EventName
    private String name;

    private EventType eventType;

    @EventYear
    private Integer year;

    @MaxPoints
    private Integer maxPresPoints;

    @MaxPoints
    private Integer maxBuildPoints;

}
