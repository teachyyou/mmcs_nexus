package ru.sfedu.mmcs_nexus.model.payload.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UploadFileResponsePayload {

    private UUID uploadId;

}