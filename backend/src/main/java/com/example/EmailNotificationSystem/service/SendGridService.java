package com.example.EmailNotificationSystem.service;

import com.example.EmailNotificationSystem.exception.EmailSendException;
import com.example.EmailNotificationSystem.model.Email;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridService {

    private final SendGrid sendGrid;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    public String sendRawEmail(Email email) {
        Mail mail = baseMail();
        mail.setSubject(email.getSubject());

        Personalization personalization = new Personalization();
        for (String to : email.getToEmailList()) {
            personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(to.trim()));
        }

        mail.addPersonalization(personalization);

        Content content = new Content(
                email.isHtml() ? "text/html" : "text/plain",
                email.getBody());

        mail.addContent(content);
        return sendMail(mail, email.getId());
    }

    public String sendTemplateMail(Email email , Map<String , String> variables){
        Mail mail = baseMail();
        mail.setSubject(email.getSubject());

        Personalization personalization = new Personalization();
        for (String to : email.getToEmailList()) {
            personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(to.trim()));
        }

        if(variables != null){
            variables.forEach(personalization::addDynamicTemplateData);
        }

        mail.addPersonalization(personalization);
        return sendMail(mail, email.getId());
    }

    public String sendRenderedTemplateMail(Email email){
        Mail mail = baseMail();
        mail.setSubject(email.getSubject());

        Personalization personalization = new Personalization();
        for (String to : email.getToEmailList()) {
            personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(to.trim()));
        }

        mail.addPersonalization(personalization);
        Content content = new Content("text/html" , email.getBody());
        mail.addContent(content);
        return sendMail(mail, email.getId());
    }

    public String sendTestEmail(Email email) {
        Mail mail = baseMail();
        mail.setSubject("[TEST] " + email.getSubject());

        Personalization p = new Personalization();
        p.addTo(new com.sendgrid.helpers.mail.objects.Email(email.getFromEmail()));
        mail.addPersonalization(p);

        mail.addContent(new Content(
                email.isHtml() ? "text/html" : "text/plain",
                buildTestWrapper(email.getBody(), email.isHtml())
        ));

        return sendMail(mail, email.getId());
    }

    public String sendMail(Mail mail , UUID emailId){
        try{
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            log.info("SendGrid response: status={}, emailId={}", response.getStatusCode(), emailId);

            if(response.getStatusCode() < 200 || response.getStatusCode() >= 300){
                throw new EmailSendException(
                        "SendGrid returned status " + response.getStatusCode() + ": " + response.getBody()
                );
            }

            String messageId = response.getHeaders().get("X-Message-Id");

            log.info("Email accepted by SendGrid: emailId={}, messageId={}", emailId, messageId);

            return messageId;
        }catch(IOException ex){

            log.error("IO error sending email emailId={}", emailId, ex);

            throw new EmailSendException("Network error communicating with SendGrid");
        }
    }

    private Mail baseMail() {
        Mail mail = new Mail();
        mail.setFrom(new com.sendgrid.helpers.mail.objects.Email(fromEmail, fromName));
        return mail;
    }

    private String buildTestWrapper(String body, boolean isHtml) {
        if (!isHtml) return "=== THIS IS A TEST EMAIL ===\n\n" + body;
        return "<div style=\"background:#fff3cd;border:1px solid #ffc107;padding:10px 16px;margin-bottom:16px;border-radius:4px;font-family:sans-serif;font-size:13px;\">"
                + "⚠️ <strong>TEST EMAIL</strong> — This message was sent as a test and was not delivered to the original recipients."
                + "</div>" + body;
    }
}
