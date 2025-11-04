package ru.sfedu.mmcs_nexus.model.payload.admin;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectJuryEventResponsePayload {

    private List<UserDTO> willingJuries;
    private List<UserDTO> obligedJuries;
    private List<UserDTO> mentors;

    public ProjectJuryEventResponsePayload() {
        this.willingJuries = new ArrayList<>();
        this.obligedJuries = new ArrayList<>();
        this.mentors = new ArrayList<>();
    }
}
