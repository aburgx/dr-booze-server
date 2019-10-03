package data.entities;

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
    /**
     * the unique id
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * the user of the verification token
     */
    @OneToOne(mappedBy = "verificationToken", cascade = CascadeType.MERGE)
    private UserBO user;

    /**
     * the token for the verification
     */
    private String token;

    /**
     * the expiry date of the token
     */
    @Temporal(value = TemporalType.DATE)
    private Date expiryDate;

    public VerificationToken() {
    }

    public VerificationToken(UserBO user, boolean useAsPin) {
        this.user = user;
        if (useAsPin) {
            // generate the pin for a reset
            this.token = String.valueOf((int) Math.floor(100000 + Math.random() * 900000));
            // setup the expiration date of the pin
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, 5);
            this.expiryDate = calendar.getTime();
        } else {
            // generate the unique token
            this.token = UUID.randomUUID().toString();
        }
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
