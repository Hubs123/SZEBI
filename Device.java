import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Device {
    private Integer id;
    private String name;
    private DeviceType type;
    private Integer roomId = null;
    private Map<String, Float> states = new HashMap<>();

    public Device(Integer id, String name, DeviceType type, Integer roomId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.roomId = roomId;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeviceType getType() {
        return type;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Map<String, Float> getStates() {
        return Collections.unmodifiableMap(new HashMap<>(states));
    }

    public Boolean applyCommand(Map<String, Float> m) {
        //TODO: przypomnieć sobie jaki był zamysł tej metody
        return null;
    }

    public void tick() {
        //TODO: tej też
    }
}
