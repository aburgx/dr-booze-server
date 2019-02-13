package transferObjects;

import enums.DrinkType;

public class DrinkVO {

    private long id;
    private DrinkType type;
    private int unixTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DrinkType getType() {
        return type;
    }

    public void setType(DrinkType type) {
        this.type = type;
    }

    public int getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(int unixTime) {
        this.unixTime = unixTime;
    }
}
