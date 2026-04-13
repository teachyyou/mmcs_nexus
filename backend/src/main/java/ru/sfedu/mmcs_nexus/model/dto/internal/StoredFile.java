package ru.sfedu.mmcs_nexus.model.dto.internal;

public class StoredFile {

    private final String storagePath;

    public StoredFile(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
