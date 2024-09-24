package ru.sfedu.mmcs_nexus.project;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ProjectJuryKey implements Serializable  {

    @Column(name="project_id")
    Long projectId;

    @Column(name="jury_id")
    Long juryId;



}
