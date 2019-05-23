package entities;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Booze_Challenges")
public class ChallengeBO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    private Template template;
    @ManyToOne
    private UserBO userBO;
    @ElementCollection
    private List<Integer> parameter;
    @Temporal(TemporalType.DATE)
    @Column(name = "createDate")
    private Date date;
    private boolean success;

    public ChallengeBO() {
        this.parameter = new ArrayList<>();
        this.date = new Date();
    }


    public ChallengeBO(UserBO userBO) {
        this.userBO = userBO;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public UserBO getUserBO() {
        return userBO;
    }

    public void setUserBO(UserBO userBO) {
        this.userBO = userBO;
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

    @Transient
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
        return "ChallengeBO{" +
                "id=" + id +
                ", template=" + template.toString() +
                ", userBO=" + userBO +
                ", parameter=" + parameter +
                ", date=" + date +
                ", success=" + success +
                '}';
    }
}
