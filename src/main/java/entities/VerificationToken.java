package entities;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity(name = "Booze_VerificationToken")
@NamedQueries({
        @NamedQuery(name = "VerificationToken.verify", query = "SELECT COUNT(v) FROM Booze_VerificationToken v WHERE v.token = :token"),
        @NamedQuery(name = "VerificationToken.getUser", query = "SELECT v.user FROM Booze_VerificationToken v WHERE v.token = :token")
})
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    private User user;

    @Column(unique = true)
    private String token;

    @Temporal(value = TemporalType.DATE)
    private Date expiryDate;

    public VerificationToken() {
    }

    public VerificationToken(User user) {
        this.user = user;
        // generate the unique token
        this.token = UUID.randomUUID().toString();
        // setup the expiration date of the token
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        this.expiryDate = calendar.getTime();
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

}
