package com.gotocompany.dagger.core.processors.external;

import com.gotocompany.dagger.core.processors.external.es.EsSourceConfig;
import com.gotocompany.dagger.core.processors.external.grpc.GrpcSourceConfig;
import com.gotocompany.dagger.core.processors.external.http.HttpSourceConfig;
import com.gotocompany.dagger.core.processors.external.pg.PgSourceConfig;
import com.gotocompany.dagger.core.processors.types.SourceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that holds external post processor configuration.
 */
public class ExternalSourceConfig {
    /**
     * The list of HTTP external lookup configurations, or {@code null} when none are configured.
     */
    private List<HttpSourceConfig> http;
    /**
     * The list of Elasticsearch external lookup configurations, or {@code null} when none are configured.
     */
    private List<EsSourceConfig> es;
    /**
     * The list of Postgres external lookup configurations, or {@code null} when none are configured.
     */
    private List<PgSourceConfig> pg;
    /**
     * The list of gRPC external lookup configurations, or {@code null} when none are configured.
     */
    private List<GrpcSourceConfig> grpc;

    /**
     * Instantiates a new External source config.
     *
     * @param http the http
     * @param es   the es
     * @param pg   the pg
     * @param grpc the grpc
     */
    public ExternalSourceConfig(List<HttpSourceConfig> http, List<EsSourceConfig> es, List<PgSourceConfig> pg, List<GrpcSourceConfig> grpc) {
        this.http = http;
        this.es = es;
        this.pg = pg;
        this.grpc = grpc;
    }

    /**
     * Gets http config.
     *
     * @return the http config
     */
    public List<HttpSourceConfig> getHttpConfig() {
        return http == null ? new ArrayList<>() : http;
    }

    /**
     * Gets es config.
     *
     * @return the es config
     */
    public List<EsSourceConfig> getEsConfig() {
        return es == null ? new ArrayList<>() : es;
    }

    /**
     * Gets pg config.
     *
     * @return the pg config
     */
    public List<PgSourceConfig> getPgConfig() {
        return pg == null ? new ArrayList<>() : pg;
    }

    /**
     * Gets grpc config.
     *
     * @return the grpc config
     */
    public List<GrpcSourceConfig> getGrpcConfig() {
        return grpc == null ? new ArrayList<>() : grpc;
    }


    /**
     * Check if the external post processor config is empty.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return (http == null || http.isEmpty()) && (es == null || es.isEmpty()) && (pg == null || pg.isEmpty()) && (grpc == null || grpc.isEmpty());
    }

    /**
     * Gets output column names.
     *
     * @return the output column names
     */
    public List<String> getOutputColumnNames() {
        ArrayList<String> columnNames = new ArrayList<>();
        columnNames.addAll(getOutputColumnNames(http));
        columnNames.addAll(getOutputColumnNames(es));
        columnNames.addAll(getOutputColumnNames(pg));
        columnNames.addAll(getOutputColumnNames(grpc));
        return columnNames;
    }

    /**
     * Collects the output column names contributed by each source in the given list.
     *
     * @param <T>     the concrete {@link SourceConfig} type held in the list
     * @param configs the source configurations to read output columns from; may be {@code null}
     * @return the aggregated output column names, or an empty list when {@code configs} is {@code null}
     */
    private <T extends SourceConfig> ArrayList<String> getOutputColumnNames(List<T> configs) {
        ArrayList<String> columnNames = new ArrayList<>();
        if (configs == null) {
            return columnNames;
        }
        configs.forEach(config -> columnNames.addAll(config.getOutputColumns()));
        return columnNames;
    }

}
