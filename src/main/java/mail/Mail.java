package mail;

import entities.UserBO;
import entities.VerificationToken;
import utils.Constants;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static javax.mail.Message.RecipientType;

public class Mail {

    private String emailPassword;
    private Session session;

    public Mail() {
        // load the email password from the config file
        try (InputStream input = new FileInputStream("src/main/resources/properties/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            emailPassword = prop.getProperty("email_password");

            // setup properties
            Properties properties = System.getProperties();
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            // setup mail session
            session = Session.getDefaultInstance(properties, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendConfirmation(UserBO user, VerificationToken verificationToken) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipients(RecipientType.TO, String.valueOf(new InternetAddress(user.getEmail())));
            message.setSubject("Welcome to Dr. Booze");
            String mailBody =
                    "<h1>Welcome to Dr. Booze</h1><br>" +
                            "<a href='" + Constants.BASE_URI + "/auth/verify/" + verificationToken.getToken()
                            + "'>Confirm your email</a>";
            transport(message, mailBody);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    public void resetPasswordConfirmation(UserBO user, int pin) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipients(RecipientType.TO, String.valueOf(new InternetAddress(user.getEmail())));
            message.setSubject("Reset your password");
            String mailBody =
                    "<h1>Your pin to reset the password</h1><br>" + "<p>The pin is " + pin + "</p>";
            transport(message, mailBody);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    private void transport(Message message, String mailBody) throws MessagingException {
        message.setContent(mailBody, "text/html");

        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.gmail.com", "dr.boozeteam@gmail.com", emailPassword);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

}
