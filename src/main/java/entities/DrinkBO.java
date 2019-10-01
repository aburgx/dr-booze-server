package entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Booze_Drink")
@NamedQueries({
        @NamedQuery(name = "Drink.get-drinks-in-between-time",
                query = "SELECT d from DrinkBO d where d.user.id = :id and (d.drankDate between :start and current_date)"),
})
public class DrinkBO {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private UserBO user;

    @ManyToOne
    private Alcohol alcohol;

    @Temporal(TemporalType.TIMESTAMP)
    private Date drankDate;

    private BigDecimal longitude;
    private BigDecimal latitude;

    public DrinkBO() {
    }

    public DrinkBO(UserBO user, Alcohol alcohol, Date drankDate, BigDecimal longitude, BigDecimal latitude) {
        this.user = user;
        this.alcohol = alcohol;
        this.drankDate = drankDate;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserBO getUser() {
        return user;
    }

    public void setUser(UserBO user) {
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