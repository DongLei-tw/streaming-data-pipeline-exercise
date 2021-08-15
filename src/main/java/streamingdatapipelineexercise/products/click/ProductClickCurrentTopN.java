package streamingdatapipelineexercise.products.click;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;
import streamingdatapipelineexercise.examples.click.shared.Config;
import streamingdatapipelineexercise.examples.click.shared.WindowClickRecord;
import streamingdatapipelineexercise.products.click.shared.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static org.apache.flink.table.api.Expressions.$;

/**
 * Create table in postgresql
 *
 * CREATE TABLE product_click (
 *      itemId VARCHAR PRIMARY KEY,
 *      description VARCHAR,
 *      "count" BIGINT,
 *      startTime timestamp,
 *      endTime timestamp
 * )
 */
public class ProductClickCurrentTopN {

    final long checkPointIntervalMs = 5000;
    final long slidingWindowSizeMs = 10_000;
    final long slidingWindowIntervalMs = 5_000;
    final int topN = 3;


    public static void main(String[] args) throws Exception {
        new ProductClickCurrentTopN().execute();
    }

    private void execute() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", Config.KAFKA_BOOTSTRAP_SERVERS);
        properties.setProperty("group.id", "ProductKeyedTopN");

        final String kafkaTopic = "product_click_avro";

        var env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(checkPointIntervalMs);

        var eventSteamOperator = ProductStreamBuilder.getClickStreamOperator(properties, kafkaTopic, env);


        SingleOutputStreamOperator<WindowClickRecord> clickWindowStream = eventSteamOperator
                .assignTimestampsAndWatermarks(
                        // define how to generate watermark
                        WatermarkStrategy
                                .<ProductClickEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                )
                .keyBy(ProductClickEvent::getItemId)
                .window(SlidingProcessingTimeWindows.of(Time.milliseconds(slidingWindowSizeMs), Time.milliseconds(slidingWindowIntervalMs)))
                .reduce(
                        (ReduceFunction<ProductClickEvent>) (e1, e2) -> e1.merge(e2),
                        new ProcessWindowFunction<>() {
                            @Override
                            public void process(String s, Context context, Iterable<ProductClickEvent> elements, Collector<WindowClickRecord> out) {
                                var record = elements.iterator().next();
                                var window = context.window();
                                var start = Timestamp.from(Instant.ofEpochMilli(window.getStart()));
                                var end = Timestamp.from(Instant.ofEpochMilli(window.getEnd()));
                                out.collect(new WindowClickRecord(record.getItemId(), record.getCount(), start, end));
                            }
                        }
                );

        var streamTableEnvironment = StreamTableEnvironment.create(env);
        var topNTable = streamTableEnvironment.fromDataStream(clickWindowStream)
                .orderBy(
                        $("endTime").desc(),
                        $("count").desc())
                .limit(topN);

        topNTable.printSchema();

        streamTableEnvironment.toChangelogStream(topNTable)
                .print("topNTable-toChangelogStream");


        var productStream = ProductStreamBuilder
                .getProductStreamOperator(properties, "product", env)
                .keyBy(Product::getItemId)
                .reduce((ReduceFunction<Product>) (pre, next) -> next);
        productStream.print("productStream");
        var productTable = streamTableEnvironment.fromDataStream(
                productStream,
                Schema.newBuilder().primaryKey("itemId").build()
        );
        productTable.printSchema();

        // join table
        var rightTable = productTable.renameColumns($("itemId").as("product_itemId"));
        rightTable.printSchema();
        streamTableEnvironment.toChangelogStream(rightTable).print("productTable-toChangelogStream");
        var joinedTable = topNTable
                .join(rightTable)
                .where($("itemId").isEqual($("product_itemId")))
                .select(
                        $("itemId"),
                        $("description"),
                        $("count"),
                        $("startTime"),
                        $("endTime")
                );
        joinedTable.printSchema();

        streamTableEnvironment.toChangelogStream(joinedTable).print("joinedTable-toChangelogStream");

        final String tableName = "product_click";
        final String jdbcURL = Config.JDBC_URL;
        final String username = Config.JDBC_USERNAME;
        final String password = Config.JDBC_PASSWORD;
        final String dbTableName = "product_click";

        var createTableStatement = "CREATE TABLE " + tableName +
                " (\n" +
                "  itemId VARCHAR PRIMARY KEY,\n" +
                "  description VARCHAR,\n" +
                "  `count` BIGINT,\n" +
                "  startTime TIMESTAMP,\n" +
                "  endTime TIMESTAMP \n" +
                ")";
        var statement = createTableStatement +
                " WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = '" + jdbcURL + "',\n" +
                "   'table-name' = '" + dbTableName + "',\n" +
                "   'username' = '" + username + "',\n" +
                "   'password' = '" + password + "'\n" +
                ")";

        streamTableEnvironment.executeSql(statement);

        joinedTable.executeInsert(tableName);
    }
}
