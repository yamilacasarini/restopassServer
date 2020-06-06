package restopass.utils;

import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
            mail.setMailContent(geContentFromTemplate(mail.getMailTempate(), mail.getModel()));
            mimeMessageHelper.setText(mail.getMailContent(), true);

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
            mail.setMailContent(geContentFromTemplate(mail.getMailTempate(), mail.getModel()));
            mimeMessageHelper.setText(mail.getMailContent(), true);

            mailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
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


