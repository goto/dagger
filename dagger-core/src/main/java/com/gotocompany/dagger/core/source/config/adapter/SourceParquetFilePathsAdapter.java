package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Gson {@link TypeAdapter} that reads the configured Parquet source file paths into a trimmed
 * {@code String[]} while a {@code StreamConfig} is deserialized.
 *
 * <p>It is wired in through a {@code @JsonAdapter} annotation on the stream config's
 * {@code parquetFilePaths} field. Reading delegates to a plain {@link Gson} instance to parse the JSON
 * array and then trims surrounding whitespace from every entry, so stray spaces in the configuration do
 * not corrupt the file URLs. Serialization is intentionally a no-op because these paths are never
 * written back out.
 */
public class SourceParquetFilePathsAdapter extends TypeAdapter<String[]> {
    /**
     * No-op serializer; Parquet file paths are read-only configuration and are never written to JSON.
     *
     * @param jsonWriter the writer (unused)
     * @param strings    the file paths (unused)
     */
    @Override
    public void write(JsonWriter jsonWriter, String[] strings) {
    }

    /**
     * Reads the JSON array of Parquet file paths and returns them trimmed of surrounding whitespace.
     *
     * @param jsonReader the reader positioned at the JSON array of file paths
     * @return a new {@code String[]} containing each configured path with leading and trailing
     *         whitespace removed
     * @throws IOException if reading from the underlying JSON stream fails
     */
    @Override
    public String[] read(JsonReader jsonReader) throws IOException {
        Gson gson = new Gson();
        String[] filePathArray = gson.fromJson(jsonReader, String[].class);
        return Arrays.stream(filePathArray)
                .map(String::valueOf)
                .map(String::trim).toArray(String[]::new);
    }
}
