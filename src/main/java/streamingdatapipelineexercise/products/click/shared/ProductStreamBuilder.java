package streamingdatapipelineexercise.products.click.shared;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import streamingdatapipelineexercise.examples.click.shared.Config;

import java.util.Properties;

public class ProductStreamBuilder {

    public static SingleOutputStreamOperator<ProductClickEvent> getClickStreamOperator(
            Properties properties,
            String kafkaTopic,
            StreamExecutionEnvironment env) {
        var expectedContent = getSchemaContent();

        var expectedSchema = new Schema.Parser().parse(expectedContent);

        var schema = ConfluentRegistryAvroDeserializationSchema.forGeneric(
                expectedSchema,
                Config.SCHEMA_REGISTRY_SERVER
        );

        var stream = env.addSource(new FlinkKafkaConsumer<>(kafkaTopic, schema, properties));

        var operator = stream.map((MapFunction<GenericRecord, ProductClickEvent>) value -> {
            String itemId = ((Utf8) value.get("itemId")).toString();
            long count = (long) value.get("count");
            String description = ((Utf8) value.get("description")).toString();
            long eventTime = (long) value.get("eventTime");

            return new ProductClickEvent(itemId, description, count, eventTime);
        });

        return operator;
    }

    private static String getSchemaContent() {

        var schemaString = "{\n" +
                "      \"type\": \"record\",\n" +
                "      \"name\": \"product_click\",\n" +
                "      \"fields\": [\n" +
                "          {\n" +
                "              \"name\": \"itemId\",\n" +
                "              \"type\": \"string\"\n" +
                "          },\n" +
                "          {\n" +
                "              \"name\": \"description\",\n" +
                "              \"type\": \"string\"\n" +
                "          },\n" +
                "          {\n" +
                "              \"name\": \"count\",\n" +
                "              \"type\": \"long\"\n" +
                "          },\n" +
                "          {\n" +
                "              \"name\": \"eventTime\",\n" +
                "              \"type\" : \"long\",\n" +
                "              \"logicalType\": \"timestamp-millis\"\n" +
                "          }\n" +
                "      ]\n" +
                "    }";

        return schemaString;
    }
}
