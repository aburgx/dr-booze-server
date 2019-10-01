package transferObjects;

import java.math.BigDecimal;
import java.util.Date;

public class DrinkVO {
    private long alcoholId;
    private Date drankDate;
    private BigDecimal longitude;
    private BigDecimal latitude;

    public DrinkVO() {
    }

    public long getAlcoholId() {
        return alcoholId;
    }

    public void setAlcoholId(long alcoholId) {
        this.alcoholId = alcoholId;
    }

    public Date getDrankDate() {
        return drankDate;
    }

    public void setDrankDate(Date drankDate) {
        this.drankDate = drankDate;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
}