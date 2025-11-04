package ru.sfedu.mmcs_nexus.model.internal;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class GradeTableRow {

    private UUID projectId;
    private UUID mentorId;
    private String projectDisplayName;

    //maps jury to its grade in table
    private List<GradeDTO> tableRow;

    public GradeTableRow(UUID projectId, UUID mentorId, String projectDisplayName) {
        this.projectDisplayName=projectDisplayName;
        this.mentorId=mentorId;
        this.projectId=projectId;
    }

}
