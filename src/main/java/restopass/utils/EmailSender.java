package restopass.utils;

import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import restopass.dto.EmailModel;

@Component
public class EmailSender {

    private static JavaMailSender mailSender;
    private static Configuration fmConfiguration;

    @Autowired
    public EmailSender(JavaMailSender mailSender, Configuration fmConfiguration) {
        EmailSender.mailSender = mailSender;
        EmailSender.fmConfiguration = fmConfiguration;
    }

    public static void sendEmail(EmailModel mail) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setSubject(mail.getSubject());
            mimeMessageHelper.setTo(mail.getEmailTo());

            MimeMessage message = mimeMessageHelper.getMimeMessage();

            MimeMultipart multipart = new MimeMultipart();

            BodyPart messageBodyPart = new MimeBodyPart();
            String htmlText = geContentFromTemplate(mail.getMailTempate(), mail.getModel());
            messageBodyPart.setContent(htmlText, "text/html");

            if(mail.getModel().get("qrCode") != null) {
                String body = mail.getModel().get("qrCode").toString().replace("data:image/jpeg;base64", "");
                MimeBodyPart filePart = new PreencodedMimeBodyPart("base64");
                filePart.setFileName("imageQr.jpeg");
                filePart.setText(body);
                filePart.setHeader("Content-ID", "<imageQr>");

                multipart.addBodyPart(filePart);
            }

            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            mailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void sendMultipleEmails(EmailModel mail, String addresses) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setSubject(mail.getSubject());
            mimeMessage.setRecipients(Message.RecipientType.CC, addresses);

            MimeMessage message = mimeMessageHelper.getMimeMessage();

            MimeMultipart multipart = new MimeMultipart();

            BodyPart messageBodyPart = new MimeBodyPart();
            String htmlText = geContentFromTemplate(mail.getMailTempate(), mail.getModel());
            messageBodyPart.setContent(htmlText, "text/html");

            MimeBodyPart qrBodyPart = new MimeBodyPart();

            String body = mail.getModel().get("qrCode").toString().replace("data:image/jpeg;base64","");
            MimeBodyPart filePart = new PreencodedMimeBodyPart("base64");
            filePart.setFileName("imageQr.jpeg");
            filePart.setText(body);
            filePart.setHeader("Content-ID", "<imageQr>");

            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(filePart);

            message.setContent(multipart);

            mailSender.send(mimeMessageHelper.getMimeMessage());

        } catch (MessagingException e ) {
            e.printStackTrace();
        }
    }

    public static String geContentFromTemplate(String mailTemplate, Map < String, Object > model) {
        StringBuffer content = new StringBuffer();

        try {
            content.append(FreeMarkerTemplateUtils
                    .processTemplateIntoString(fmConfiguration.getTemplate(mailTemplate), model));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}


