package streamingdatapipelineexercise.products.click.shared;

public class Product {
    private String itemId;
    private String description;
    private long count;

    public Product() {
    }

    public Product(String itemId, String description, long count) {
        this.itemId = itemId;
        this.description = description;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Product{" +
                "itemId='" + itemId + '\'' +
                ", description='" + description + '\'' +
                ", count=" + count +
                '}';
    }
}
