package ru.sfedu.mmcs_nexus.model.payload.jury;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Setter
@Getter
public class CreateGradeRequestPayload {

    @NotNull(message="project UUID is required")
    private UUID projectId;

    @NotNull(message="event UUID is required")
    private UUID eventId;

    @Length(max=1024, message = "comment: length limit is 1024")
    private String comment;

    @Min(value = 0, message = "Presentation points cannot be negative")
    @Max(value = 99, message = "Presentation points cannot be greater than 99")
    private Integer presPoints;

    @Min(value = 0, message = "Build points cannot be negative")
    @Max(value = 99, message = "Build points cannot be greater than 99")
    private Integer buildPoints;


}
