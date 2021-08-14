package streamingdatapipelineexercise.products.click.shared;

public class ProductClickEvent {

    private String itemId;
    private String description;
    private long count;
    private long timestamp;

    public ProductClickEvent() {
    }

    public ProductClickEvent(
            String itemId,
            String description,
            long count,
            long timestamp) {
        this.itemId = itemId;
        this.description = description;
        this.count = count;
        this.timestamp = timestamp;
    }


    public String getItemId() {
        return itemId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ProductClickEvent{" +
                "itemId='" + itemId + '\'' +
                ", description='" + description + '\'' +
                ", count=" + count +
                ", timestamp=" + timestamp +
                '}';
    }

    public ProductClickEvent merge(ProductClickEvent event) throws Exception {
        if (event.getItemId() != this.itemId) {
            throw new Exception("Different Product Click Event Can't Be Merged.");
        }

        long count = this.count + event.getCount();
        long ts = Math.max(this.timestamp, event.getTimestamp());

        return new ProductClickEvent(this.itemId, this.description, count, ts);
    }
}
