package entities;

import enums.DrinkType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Booze_Drink")
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
    private float longitude;
    private float latitude;

    public DrinkBO() {
    }

    public DrinkBO(UserBO user, DrinkType type, Date drankDate,
                   String name, double percentage, int amount, float longitude, float latitude) {
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

    public Date getDate() {
        return drankDate;
    }

    public void setDate(Date drankDate) {
        this.drankDate = drankDate;
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

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
}
