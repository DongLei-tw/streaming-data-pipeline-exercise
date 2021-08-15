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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
