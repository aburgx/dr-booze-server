package entities;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Booze_VerificationToken")
@NamedQueries({
        @NamedQuery(name = "Token.get-by-token", query = "SELECT v FROM VerificationToken v WHERE v.token = :token")
})
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(mappedBy = "verificationToken", cascade = CascadeType.MERGE)
    private UserBO user;

    private String token;

    @Temporal(value = TemporalType.DATE)
    private Date expiryDate;

    public VerificationToken() {
    }

    public VerificationToken(UserBO user, boolean usePin) {
        this.user = user;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (usePin) {
            // generate the pin for a reset
            this.token = String.valueOf((int) Math.floor(100000 + Math.random() * 900000));
            calendar.add(Calendar.MINUTE, 5);
        } else {
            // generate the unique token
            this.token = UUID.randomUUID().toString();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // setup the expiration date of the token
        this.expiryDate = calendar.getTime();

    }

    public long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UserBO getUser() {
        return user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

}
