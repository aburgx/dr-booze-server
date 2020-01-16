package data.entities;

import data.enums.AlcoholType;
import org.json.JSONObject;

import javax.persistence.*;

@Entity
@Table(name = "Booze_Alcohol")
public class Alcohol {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private AlcoholType type;

    private String name;
    private float percentage;
    private int amount;
    private String category;

    /**
     * A boolean indicating if a personal alcohol was removed by an user.
     */
    private boolean isArchived = false;

    public Alcohol() {
    }

    public Alcohol(AlcoholType type, String name, float percentage, int amount) {
        this.type = type;
        this.name = name;
        this.percentage = percentage;
        this.amount = amount;
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("id", id)
                .put("type", type.toString())
                .put("name", name)
                .put("percentage", percentage)
                .put("amount", amount)
                .put("category", category);
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

    public AlcoholType getType() {
        return type;
    }

    public void setType(AlcoholType type) {
        this.type = type;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }
}
