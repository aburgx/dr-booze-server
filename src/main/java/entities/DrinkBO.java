package entities;

import enums.DrinkType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "Booze_Drink")
@NamedQueries({
        @NamedQuery(name = "Drink.get-drinks-in-between-time", query = "SELECT d from DrinkBO d where d.user.id = :id and (d.drankDate between :start and current_date)"),
})
public class DrinkBO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private UserBO user;

    @Enumerated(value = EnumType.STRING)
    private DrinkType type;

    @Temporal(value = TemporalType.DATE)
    private Date drankDate;

    private String name;
    private double percentage;
    private int amount;

    private BigDecimal longitude;
    private BigDecimal latitude;

    public DrinkBO() {
    }

    public DrinkBO(UserBO user, DrinkType type, Date drankDate,
                   String name, double percentage, int amount,
                   BigDecimal longitude, BigDecimal latitude) {
        this.user = user;
        this.drankDate = drankDate;
        this.type = type;
        this.name = name;
        this.percentage = percentage;
        this.amount = amount;
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

    public DrinkType getType() {
        return type;
    }

    public void setType(DrinkType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDrankDate() {
        return drankDate;
    }

    public void setDrankDate(Date drankDate) {
        this.drankDate = drankDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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
