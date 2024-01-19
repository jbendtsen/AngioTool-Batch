package AngioTool;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AASendEmail {
   private boolean gmail = true;
   private boolean hotmail = false;
   private boolean nih = false;
   private static final String SMTP_HOST_NAME = "smtp.gmail.com";
   private static final String SMTP_PORT = "465";
   private static final String emailMsgTxt = "Test Message Contents";
   private static final String emailSubjectTxt = "A test from gmail";
   private static final String emailFromAddress = "enriquezudaire1@gmail.com";
   private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
   private static final String[] sendTo = new String[]{"enriquezudaire@hotmail.com"};

   public void sendSSLMessage(String[] recipients, String subject, String message, String from) throws MessagingException {
      if (true)
        return;
      boolean debug = false;
      Properties props = new Properties();
      props.put("mail.smtp.host", "smtp.gmail.com");
      props.put("mail.smtp.auth", "true");
      props.put("mail.debug", debug);
      props.put("mail.smtp.port", "465");
      props.put("mail.smtp.socketFactory.port", "465");
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.smtp.socketFactory.fallback", "false");
      Session session = Session.getDefaultInstance(props, new Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            if (AASendEmail.this.gmail) {
               return new PasswordAuthentication("enriquezudaire1", "Yosoytueres@4");
            } else if (AASendEmail.this.hotmail) {
               return new PasswordAuthentication("enriquezudaire", "Yosoytueres@2");
            } else {
               return AASendEmail.this.nih ? new PasswordAuthentication("zudairee", "Yosoytueres@5") : null;
            }
         }
      });
      session.setDebug(debug);
      Message msg = new MimeMessage(session);
      InternetAddress addressFrom = new InternetAddress(from);
      msg.setFrom(addressFrom);
      InternetAddress[] addressTo = new InternetAddress[recipients.length];

      for(int i = 0; i < recipients.length; ++i) {
         addressTo[i] = new InternetAddress(recipients[i]);
      }

      msg.setRecipients(RecipientType.TO, addressTo);
      msg.setSubject(subject);
      msg.setContent(message, "text/plain");
      Transport.send(msg);
   }
}
