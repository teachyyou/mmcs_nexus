package ru.sfedu.mmcs_nexus.model.payload.admin;

import ru.sfedu.mmcs_nexus.model.internal.ImportRowIssue;

import java.util.List;

public record ImportResponsePayload<T>
        (
            List<T> created,
            List<ImportRowIssue> skipped
        ) {}
