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
    private final TemplateEngine templateEngine; // dacă nu folosești template, vezi fallback
    private final MagicJwt magicJwt;
    @Value("${app.base-url}") private String baseUrl;

    public void sendMagicLink(String toEmail, Long roundId) {
        String jwt = magicJwt.create(toEmail, roundId);
        String link = baseUrl + "/magic/verify?jwt=" + jwt;

        try {
            MimeMessage m = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(m, "UTF-8");
            h.setTo(toEmail);
            h.setSubject("Invitație la runda de interviu");
            h.setText(buildText(link), buildHtml(link));
            mailSender.send(m);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send magic link to " + toEmail, e);
        }
    }

    private String buildText(String link) {
        return "Invitație la runda de interviu\nAccesează: " + link + "\n";
    }
    private String buildHtml(String link) {
        if (templateEngine != null) {
            var ctx = new Context(); ctx.setVariable("magicLink", link);
            return templateEngine.process("mail/magic-link", ctx);
        }
        return """
      <div style="font-family:Inter,Arial,sans-serif">
        <h2>Invitație la runda de interviu</h2>
        <p><a href="%s">Participă la interviu</a></p>
        <p>Sau copiază: <code>%s</code></p>
      </div>
    """.formatted(link, link);
    }
}
