package data.entities;

import data.enums.AlcoholType;

import javax.persistence.*;

@Entity
@Table(name = "Booze_Alcohol")
@NamedQueries({
        @NamedQuery(name = "Alcohol.get-with-type", query = "SELECT a FROM Alcohol a WHERE a.type = :type")
})
public class Alcohol {
    @Id
    @GeneratedValue
    private long id;

    @Enumerated(EnumType.STRING)
    private AlcoholType type;

    private String name;
    private float percentage;
    private int amount;

    public Alcohol() {
    }

    public Alcohol(AlcoholType type, String name, float percentage, int amount) {
        this.type = type;
        this.name = name;
        this.percentage = percentage;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public AlcoholType getType() {
        return type;
    }

    public void setType(AlcoholType type) {
        this.type = type;
    }
}
