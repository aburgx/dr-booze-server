package data.entities;

import org.json.JSONObject;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Booze_Drink")
@NamedQueries({
        @NamedQuery(name = "Drink.get-drinks-in-between-time",
                query = "SELECT d from Drink d where d.user.id = :id and (d.drankDate between :start and current_date)"),
})
public class Drink {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Alcohol alcohol;

    @Temporal(TemporalType.TIMESTAMP)
    private Date drankDate;

    private BigDecimal longitude;
    private BigDecimal latitude;

    public Drink() {
    }

    public Drink(User user, Alcohol alcohol, Date drankDate, BigDecimal longitude, BigDecimal latitude) {
        this.user = user;
        this.alcohol = alcohol;
        this.drankDate = drankDate;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("id", id)
                .put("alcohol", alcohol.toJson())
                .put("drankDate", drankDate.getTime())
                .put("longitude", longitude)
                .put("latitude", latitude);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Alcohol getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(Alcohol alcohol) {
        this.alcohol = alcohol;
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
