package services;

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

/**
 * @author Alexander Burghuber
 */
public class Mail {
    private String targetAddress;
    private String emailPassword;

    public Mail(String targetAddress) {
        this.targetAddress = targetAddress;
        // load the email password from the config file
        try (InputStream input = new FileInputStream("src/main/resources/properties/config.properties")){
            Properties prop = new Properties();
            prop.load(input);
            emailPassword = prop.getProperty("email_password");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendConfirmationMail() throws MessagingException {
        // setup properties
        Properties properties = System.getProperties();
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // setup mail session
        Session session = Session.getDefaultInstance(properties, null);

        // setup message
        MimeMessage message = new MimeMessage(session);
        message.addRecipients(RecipientType.TO, String.valueOf(new InternetAddress(targetAddress)));
        message.setSubject("Welcome to DrBooze");
        String mailBody = "<h1>Confirm your email</h1>";
        message.setContent(mailBody, "text/html");

        // send mail
        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.gmail.com", "dr.boozeteam@gmail.com", emailPassword);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
        System.out.println("Email Confirmation sent.");
    }

}
