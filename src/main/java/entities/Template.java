package entities;

import enums.ChallengeType;

import javax.persistence.*;

@Entity
@Table(name = "Booze_Template")
@NamedQueries({
        @NamedQuery(name = "Template.count", query = "select count(t) from Template t"),
        @NamedQuery(name = "Template.getRandomTemplate", query = "SELECT t from Template t where t.id = :id")
})
public class Template {

    /**
     * id of the template
     */
    @Id
    private long id;
    /**
     * how the template looks
     */
    private String content;
    /**
     * given Booze-points on success
     */
    private int amount;
    /**
     * ChallengeType to minimize difficulties in the validation process
     */
    @Enumerated(EnumType.STRING)
    private ChallengeType type;


    public Template(String content, int amount) {
        this.content = content;
        this.amount = amount;
    }

    public Template() {
    }

    public Template(long id, String content, int amount, ChallengeType type) {
        this.id = id;
        this.content = content;
        this.amount = amount;
        this.type = type;
    }

    public Template(long id, String content, int amount) {
        this.id = id;
        this.content = content;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ChallengeType getType() {
        return type;
    }

    public void setType(ChallengeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                '}';
    }
}
