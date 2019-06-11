package entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Booze_Beer")
public class Beer {
    /**
     * the id of the beer
     */
    @Id
    private long id;
    /**
     * the name of the beer
     */
    private String name;
    /**
     * the alcohol percentage of the beer
     */
    private double percentage;
    /**
     * the amount in ml
     */
    private int amount;

    public Beer() {
    }

    public Beer(long id, String name, double percentage, int amount) {
        this.id = id;
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int liter) {
        this.amount = liter;
    }
}
