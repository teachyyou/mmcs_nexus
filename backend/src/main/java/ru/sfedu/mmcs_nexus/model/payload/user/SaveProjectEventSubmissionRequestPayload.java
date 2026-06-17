package ru.sfedu.mmcs_nexus.model.payload.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class SaveProjectEventSubmissionRequestPayload {

    @URL
    @Size(max = 1024)
    private String presentationUrl;

    @URL
    @Size(max = 1024)
    private String repositoryUrl;

    @URL
    @Size(max = 1024)
    private String releaseUrl;

    @Size(max = 10000)
    private String comment;
}