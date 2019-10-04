package data.entities;

import data.enums.ChallengeType;

import javax.persistence.*;

@Entity
@Table(name = "Booze_ChallengeTemplate")
@NamedQueries({
        @NamedQuery(name = "Template.count", query = "select count(t) from ChallengeTemplate t"),
        @NamedQuery(name = "Template.getRandomTemplate", query = "SELECT t from ChallengeTemplate t where t.id = :id")
})
public class ChallengeTemplate {
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

    public ChallengeTemplate() {
    }

    public ChallengeTemplate(long id, String content, int amount, ChallengeType type) {
        this.id = id;
        this.content = content;
        this.amount = amount;
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
}
