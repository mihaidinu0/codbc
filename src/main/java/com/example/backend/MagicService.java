
package com.example.backend.service;

import com.example.backend.model.Candidate;
import com.example.backend.model.CandidateId;
import com.example.backend.repo.CandidateRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.security.MagicJwt;
import com.example.backend.security.SessionJwt;
import com.example.backend.security.MagicJwt.ClaimsData;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class MagicService {

    private final MagicJwt magicJwt;
    private final SessionJwt sessionJwt;
    private final UserRepository userRepo;
    private final CandidateRepository candidateRepo;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public Outcome verify(String jwt) {
        try {
            ClaimsData data = magicJwt.parse(jwt);
            String email = data.email();
            Long roundId = data.roundId();

            // user existent
            com.example.backend.model.MyUser user = userRepo.findById(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // verifică dacă e candidat
            CandidateId cid = new CandidateId(user.getEmail(), roundId);
            Candidate cand = candidateRepo.findById(cid)
                    .orElseThrow(() -> new RuntimeException("Not a candidate for this round"));

            // one-time
            if (cand.isTokenUsed()) {
                return Outcome.error(URI.create(frontendUrl + "/join/error?reason=used"));
            }
            cand.setTokenUsed(true);

            // generează session JWT și cookie
            String sessionToken = sessionJwt.create(email);
            Cookie cookie = new Cookie("session", sessionToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);   // true în producție (HTTPS)
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 3); // 3 zile

            return Outcome.success(
                    URI.create(frontendUrl + "/join/success?roundId=" + roundId),
                    cookie
            );

        } catch (JwtException e) {
            return Outcome.error(URI.create(frontendUrl + "/join/error?reason=invalid_or_expired"));
        } catch (RuntimeException e) {
            return Outcome.error(URI.create(frontendUrl + "/join/error?reason=not_candidate"));
        }
    }

    public static class Outcome {
        private final URI redirect;
        private final Cookie cookie; // null dacă e error

        private Outcome(URI redirect, Cookie cookie) {
            this.redirect = redirect;
            this.cookie = cookie;
        }

        public static Outcome success(URI redirect, Cookie cookie) {
            return new Outcome(redirect, cookie);
        }

        public static Outcome error(URI redirect) {
            return new Outcome(redirect, null);
        }

        public URI getRedirect() { return redirect; }
        public Cookie getCookie() { return cookie; }
    }
}
