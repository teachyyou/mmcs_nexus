package ru.sfedu.mmcs_nexus.service;

import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.enums.entity.ProjectType;
import ru.sfedu.mmcs_nexus.model.internal.ImportRowIssue;
import ru.sfedu.mmcs_nexus.model.payload.admin.ImportResponsePayload;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ImportService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ImportService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ImportResponsePayload<Project> ImportProjectsFromCsv(MultipartFile file, Integer limit) {

        if (file.getContentType() != null && file.getContentType().equals("text/csv")) {
            throw new MultipartException("required extension is csv");
        }

        List<Project> created = new ArrayList<Project>();
        List<ImportRowIssue> skipped = new ArrayList<ImportRowIssue>();

        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             var csv = CSVParser.parse(
                     reader,
                     CSVFormat.RFC4180
                             .builder()
                             .setHeader()
                             .setSkipHeaderRecord(true)
                             .setIgnoreSurroundingSpaces(true)
                             .setTrim(true)
                             .build()
             )) {

            int count = 0;
            for (CSVRecord r : csv) {
                if (limit > 0 && count >= limit) break;
                try {
                    Project p = mapRecordToProject(r);

                    if (projectRepository.existsByName(p.getName())) {
                        skipped.add(ImportRowIssue.of(r.getRecordNumber(), "duplicate_name",
                                "Project with name '%s' already exists".formatted(p.getName())));
                        continue;
                    }

                    projectRepository.save(p);
                    created.add(p);
                    count++;
                } catch (IllegalArgumentException ex) {
                    skipped.add(ImportRowIssue.of(r.getRecordNumber(), "bad_record", ex.getMessage()));
                } catch (DataIntegrityViolationException ex) {
                    skipped.add(ImportRowIssue.of(r.getRecordNumber(), "db_constraint", ex.getMessage()));
                } catch (Exception ex) {
                    skipped.add(ImportRowIssue.of(r.getRecordNumber(), "unexpected", ex.getMessage()));
                }
            }
        } catch (Exception e) {
            return new ImportResponsePayload<Project>(List.of(), List.of(ImportRowIssue.of(0, "read_error", e.getMessage())));
        }

        return new ImportResponsePayload<Project>(created, skipped);
    }

    private Project mapRecordToProject(CSVRecord r) {
        String name          = get(r, "name", true);
        String description   = get(r, "projectDescription", false);
        String type = mapProjectType(get(r, "projectType", true));
        //Integer year         = parseInt(get(r, "year", true), "year");
        int year         = Year.now().getValue();

        Integer externalId         = parseInt(get(r, "id", false), "externalId");
        Integer qtyStudents        = parseInt(get(r, "quantityOfStudents", false, "quantity_of_students"), "quantityOfStudents");
        String captainName         = get(r, "captainFio", true, "captain_name");
        String track               = get(r, "track", true);
        String technologies        = get(r, "technologies", false);
        Boolean full               = parseBool(get(r, "isFull", false, "is_full"));

        return new Project(
                externalId,
                qtyStudents,
                captainName,
                full != null ? full : false,
                track,
                technologies,
                name,
                description,
                type,
                year
        );
    }

    private static String mapProjectType(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Required column 'projectType' is missing or empty");
        }
        String key = normalize(s);
        return switch (key) {
            case "web", "webapp", "webapplication", "site" -> ProjectType.WEB_APP.name();
            case "desktop", "desktopapp", "desktopapplication" -> ProjectType.DESKTOP_APP.name();
            case "mobile", "mobileapp", "android", "ios" -> ProjectType.MOBILE_APP.name();
            case "game", "videogame" -> ProjectType.GAME.name();
            case "gamemod", "mod", "modification" -> ProjectType.GAME_MOD.name();
            case "bot", "telegrambot", "tg", "tgbot" -> ProjectType.TELEGRAM_BOT.name();
            default -> throw new IllegalArgumentException(STR."Unknown projectType value: \{s}");
        };
    }

    private static String get(CSVRecord r, String key, boolean required, String... aliases) {
        Map<String, String> map = r.toMap();
        if (map == null) throw new IllegalArgumentException("Empty CSV row");

        String found = findKey(map, key);
        if (found == null) {
            for (String a : aliases) {
                found = findKey(map, a);
                if (found != null) break;
            }
        }
        String val = (found != null) ? map.get(found) : null;
        if (required && (val == null || val.isBlank())) {
            throw new IllegalArgumentException("Required column '%s' is missing or empty".formatted(key));
        }
        return (val != null && !val.isBlank()) ? val.trim() : null;
    }

    private static String findKey(Map<String, String> map, String name) {
        if (name == null) return null;
        String norm = normalize(name);
        for (String k : map.keySet()) {
            if (normalize(k).equals(norm)) return k;
        }
        return null;
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    private static Integer parseInt(String s, String field) {
        if (s == null) return null;
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer in '%s': %s".formatted(field, s));
        }
    }

    private static Boolean parseBool(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "true", "t", "1", "yes", "y", "да" -> true;
            case "false", "f", "0", "no", "n", "нет" -> false;
            default -> throw new IllegalArgumentException(STR."Invalid boolean value: \{s}");
        };
    }


}
