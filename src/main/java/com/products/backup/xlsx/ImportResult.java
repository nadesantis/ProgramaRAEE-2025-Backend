package com.products.backup.xlsx;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {

    private int created;
    private int updated;
    private int skipped;
    private int failed;
    private List<String> errors = new ArrayList<>();

    // ✅ AGREGA ESTO
    public ImportResult() {
        this.created = 0;
        this.updated = 0;
        this.skipped = 0;
        this.failed = 0;
        this.errors = new ArrayList<>();
    }

    public ImportResult(int created, int updated, int skipped, int failed, List<String> errors) {
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.failed = failed;
        this.errors = errors;
    }

    public void addUpsert(String type, String name) {
        this.updated++;
    }

    public void addError(String message) {
        this.failed++;
        this.errors.add(message);
    }

    public int totalUpserts() {
        return created + updated;
    }

    public List<String> errors() {
        return errors;
    }

    // Getters y setters si son necesarios
}
