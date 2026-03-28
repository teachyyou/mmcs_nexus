package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.annotation.Nullable;

import java.util.UUID;

public class UploadFileResponsePayload {

    private UUID uploadId;
    private String storagePath;
    private String publicUrl;

}
