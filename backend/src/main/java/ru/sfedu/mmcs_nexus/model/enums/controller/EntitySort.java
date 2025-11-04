package ru.sfedu.mmcs_nexus.model.enums.controller;

import java.util.Set;

public enum EntitySort {

    USER_SORT(Set.of("id","login","email","firstname","lastname","status","role")),
    PROJECT_SORT(
            Set.of(
                    "id",
                    "externalid",
                    "name",
                    "description",
                    "type",
                    "year",
                    "quantityofstudents",
                    "captainname",
                    "isfull",
                    "track",
                    "technologies"
            )
    ),

    EVENT_SORT(Set.of("id","name","eventtype","year","maxprespoints","maxbuildpoints")),
    GRADE_SORT(Set.of("id","comment","prespoints","buildpoints"));

    private final Set<String> allowed;

    EntitySort(Set<String> allowed) {
        this.allowed = Set.copyOf(allowed);
    }

    public boolean isNotAllowed(String sortParam) {
        return sortParam == null || !allowed.contains(sortParam.toLowerCase());
    }
}
