package streamingdatapipelineexercise.examples.click.shared;

public class Config {
    // local debug:
    static public String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    static public String SCHEMA_REGISTRY_SERVER = "http://localhost:18081";
    static public String JDBC_URL = "jdbc:postgresql://localhost:5432/database";

    // docker env:
//    static public String KAFKA_BOOTSTRAP_SERVERS = "kafka:19092";
//    static public String SCHEMA_REGISTRY_SERVER = "http://schema-registry:8081";
//    static public String JDBC_URL = "jdbc:postgresql://database:5432/database";

    static public String JDBC_USERNAME = "postgres";
    static public String JDBC_PASSWORD = "postgres";
}
