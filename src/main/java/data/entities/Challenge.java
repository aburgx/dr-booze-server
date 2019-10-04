package data.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Booze_Challenges")
public class Challenge {
    /**
     * id of the challenge
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * template to use in the challenge
     */
    @ManyToOne
    private ChallengeTemplate template;

    /**
     * user of the challenge
     */
    @ManyToOne
    private User user;

    /**
     * parameter list of the challenge parameters
     */
    @ElementCollection
    private List<Integer> parameter;

    /**
     * Date when the challenge was created
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "createDate")
    private Date date;

    /**
     * was the challenge a success
     */
    private boolean success;

    public Challenge() {
        this.parameter = new ArrayList<>();
        this.date = new Date();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("desc", this.template.getContent());
        json.put("amount", this.template.getAmount());
        JSONArray arr = new JSONArray();
        for (Integer p : this.parameter) {
            arr.put(new JSONObject().put("param", p));
        }
        json.put("params", arr);
        return json;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "id=" + id +
                ", template=" + template.toString() +
                ", user=" + user +
                ", parameter=" + parameter +
                ", date=" + date +
                ", success=" + success +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ChallengeTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ChallengeTemplate template) {
        this.template = template;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Integer> getParameter() {
        return parameter;
    }

    public void setParameter(List<Integer> parameter) {
        this.parameter = parameter;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
