package com.gotocompany.dagger.core.processors.external;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.core.processors.PostProcessorConfig;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.external.es.EsSourceConfig;
import com.gotocompany.dagger.core.processors.external.es.EsStreamDecorator;
import com.gotocompany.dagger.core.processors.external.grpc.GrpcSourceConfig;
import com.gotocompany.dagger.core.processors.external.grpc.GrpcStreamDecorator;
import com.gotocompany.dagger.core.processors.external.http.HttpSourceConfig;
import com.gotocompany.dagger.core.processors.external.http.HttpStreamDecorator;
import com.gotocompany.dagger.core.processors.external.pg.PgSourceConfig;
import com.gotocompany.dagger.core.processors.external.pg.PgStreamDecorator;
import com.gotocompany.dagger.core.processors.types.PostProcessor;
import com.gotocompany.dagger.core.processors.types.SourceConfig;
import com.gotocompany.dagger.core.processors.types.StreamDecorator;
import com.gotocompany.dagger.core.processors.types.Validator;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.util.List;

/**
 * The External post processor.
 */
public class ExternalPostProcessor implements PostProcessor {

    /**
     * The schema configuration shared with the stream decorators and async connectors created here.
     */
    private final SchemaConfig schemaConfig;
    /**
     * The configuration holding all external lookup sources (HTTP, Elasticsearch, Postgres, and gRPC).
     */
    private final ExternalSourceConfig externalSourceConfig;
    /**
     * The metric configuration applied to each external source, including the per-source metric id.
     */
    private final ExternalMetricConfig externalMetricConfig;

    /**
     * Instantiates a new External post processor.
     *
     * @param schemaConfig         the schema config
     * @param externalSourceConfig the external source config
     * @param externalMetricConfig the external metric config
     */
    public ExternalPostProcessor(SchemaConfig schemaConfig, ExternalSourceConfig externalSourceConfig, ExternalMetricConfig externalMetricConfig) {
        this.schemaConfig = schemaConfig;
        this.externalSourceConfig = externalSourceConfig;
        this.externalMetricConfig = externalMetricConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Indicates that this post-processor runs only when the post-processor configuration declares an
     * external source.
     *
     * @param postProcessorConfig the post-processor configuration to inspect
     * @return {@code true} if an external source is configured, otherwise {@code false}
     */
    @Override
    public boolean canProcess(PostProcessorConfig postProcessorConfig) {
        return postProcessorConfig.hasExternalSource();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Enriches the incoming stream by chaining an async lookup for every configured HTTP, Elasticsearch,
     * Postgres, and gRPC source, assigning each source a metric id, and returns a new {@link StreamInfo}
     * wrapping the enriched stream with the original column names.
     *
     * @param streamInfo the stream and column metadata to enrich
     * @return a new {@link StreamInfo} carrying the enriched data stream
     */
    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        DataStream<Row> resultStream = streamInfo.getDataStream();

        List<HttpSourceConfig> httpSourceConfigs = externalSourceConfig.getHttpConfig();
        for (int index = 0; index < httpSourceConfigs.size(); index++) {
            HttpSourceConfig httpSourceConfig = httpSourceConfigs.get(index);
            externalMetricConfig.setMetricId(getMetricId(index, httpSourceConfig));
            resultStream = enrichStream(resultStream, httpSourceConfig, getHttpDecorator(httpSourceConfig));
        }

        List<EsSourceConfig> esSourceConfigs = externalSourceConfig.getEsConfig();
        for (int index = 0; index < esSourceConfigs.size(); index++) {
            EsSourceConfig esSourceConfig = esSourceConfigs.get(index);
            externalMetricConfig.setMetricId(getMetricId(index, esSourceConfig));
            resultStream = enrichStream(resultStream, esSourceConfig, getEsDecorator(esSourceConfig));
        }

        List<PgSourceConfig> pgSourceConfigs = externalSourceConfig.getPgConfig();
        for (int index = 0; index < pgSourceConfigs.size(); index++) {
            PgSourceConfig pgSourceConfig = pgSourceConfigs.get(index);
            externalMetricConfig.setMetricId(getMetricId(index, pgSourceConfig));
            resultStream = enrichStream(resultStream, pgSourceConfig, getPgDecorator(pgSourceConfig));
        }

        List<GrpcSourceConfig> grpcSourceConfigs = externalSourceConfig.getGrpcConfig();
        for (int index = 0; index < grpcSourceConfigs.size(); index++) {
            GrpcSourceConfig grpcSourceConfig = grpcSourceConfigs.get(index);
            externalMetricConfig.setMetricId(getMetricId(index, grpcSourceConfig));
            resultStream = enrichStream(resultStream, grpcSourceConfig, getGrpcDecorator(grpcSourceConfig));
        }

        return new StreamInfo(resultStream, streamInfo.getColumnNames());
    }

    /**
     * Resolves the metric id for a source, falling back to its positional index when none is configured.
     *
     * @param index        the positional index of the source within its configured list
     * @param sourceConfig the source configuration to read the metric id from
     * @return the configured metric id, or the index as a string when it is empty
     */
    private String getMetricId(int index, SourceConfig sourceConfig) {
        String metricId = sourceConfig.getMetricId();
        return (StringUtils.isEmpty(metricId)) ? String.valueOf(index) : metricId;
    }

    /**
     * Validates the given source configuration and applies the decorator that attaches the async lookup.
     *
     * @param resultStream the stream to be enriched
     * @param configs      the source configuration whose fields are validated before decoration
     * @param decorator    the decorator that wires the async connector onto the stream
     * @return the decorated stream
     */
    private DataStream<Row> enrichStream(DataStream<Row> resultStream, Validator configs, StreamDecorator decorator) {
        configs.validateFields();
        return decorator.decorate(resultStream);
    }

    /**
     * Gets http decorator.
     *
     * @param httpSourceConfig the http source config
     * @return the http decorator
     */
    protected HttpStreamDecorator getHttpDecorator(HttpSourceConfig httpSourceConfig) {
        return new HttpStreamDecorator(httpSourceConfig, externalMetricConfig, schemaConfig);
    }

    /**
     * Gets es decorator.
     *
     * @param esSourceConfig the es source config
     * @return the es decorator
     */
    protected EsStreamDecorator getEsDecorator(EsSourceConfig esSourceConfig) {
        return new EsStreamDecorator(esSourceConfig, externalMetricConfig, schemaConfig);
    }

    /**
     * Builds the Postgres stream decorator for the given source configuration.
     *
     * @param pgSourceConfig the Postgres source configuration
     * @return a new {@link PgStreamDecorator} bound to the shared metric and schema configuration
     */
    private PgStreamDecorator getPgDecorator(PgSourceConfig pgSourceConfig) {
        return new PgStreamDecorator(pgSourceConfig, externalMetricConfig, schemaConfig);
    }

    /**
     * Builds the gRPC stream decorator for the given source configuration.
     *
     * @param grpcSourceConfig the gRPC source configuration
     * @return a new {@link GrpcStreamDecorator} bound to the shared metric and schema configuration
     */
    private GrpcStreamDecorator getGrpcDecorator(GrpcSourceConfig grpcSourceConfig) {
        return new GrpcStreamDecorator(grpcSourceConfig, externalMetricConfig, schemaConfig);
    }


}
