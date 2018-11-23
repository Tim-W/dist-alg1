import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OrderingBuffer implements Serializable {
    final Map<Id, VectorTimestamp> buffer;

    public OrderingBuffer() {
        buffer = new HashMap<>();
    }

    public OrderingBuffer copy() {
        OrderingBuffer bufferCopy = new OrderingBuffer();
        for (Id id : buffer.keySet()) {
            bufferCopy.buffer.put(id, buffer.get(id));
        }
        return bufferCopy;
    }

    public void put(Id id, VectorTimestamp ts) {
        buffer.put(id, ts);
    }

    public boolean contains(Id id) {
        return buffer.containsKey(id);
    }

    public VectorTimestamp get(Id id) {
        return buffer.get(id);
    }

    public void merge(OrderingBuffer other) {
        other.buffer.forEach((id, ts) -> {
            if (!buffer.containsKey(id)) {
                buffer.put(id, ts);
            } else {
                if (buffer.get(id).leq(other.buffer.get(id))) {
                    buffer.put(id, ts);
                }
            }
        });
    }

}
