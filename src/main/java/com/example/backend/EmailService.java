package com.example.backend.service;

import com.example.backend.security.MagicJwt;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // dacă nu folosești thymeleaf, poți scoate
    private final MagicJwt magicJwt;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendMagicLink(String toEmail, Long roundId) {
        String jwt = magicJwt.create(toEmail, roundId);
        String link = baseUrl + "/magic/verify?jwt=" + jwt;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Invite la runda de interviu");

            // HTML prin Thymeleaf (dacă ai template); altfel fallback inline
            String html = buildHtml(link);
            String text = buildText(link);
            helper.setText(text, html); // (text/plain, text/html)

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send magic link to " + toEmail, e);
        }
    }

    private String buildHtml(String link) {
        if (templateEngine != null) {
            Context ctx = new Context();
            ctx.setVariable("magicLink", link);
            return templateEngine.process("mail/magic-link", ctx);
        }
        return """
      <div style="font-family:Inter,Arial,sans-serif;line-height:1.5">
        <h2>Invitație la runda de interviu</h2>
        <p>Pentru a participa, apasă mai jos:</p>
        <p><a href="%s">Participă la interviu</a></p>
        <p>Dacă nu merge click, copiază în browser:<br><code>%s</code></p>
      </div>
      """.formatted(link, link);
    }

    private String buildText(String link) {
        return "Invitație la runda de interviu\n\nAccesează linkul pentru a participa:\n" + link + "\n";
    }
}
