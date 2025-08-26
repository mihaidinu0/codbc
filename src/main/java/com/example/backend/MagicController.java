package com.example.backend.controller;

import com.example.backend.model.Candidate;
import com.example.backend.model.CandidateId;
import com.example.backend.repo.CandidateRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.security.MagicJwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class MagicController {

    private final MagicJwt magicJwt;
    private final UserRepository userRepo;
    private final CandidateRepository candidateRepo;

    @GetMapping("/magic/verify")
    public String verify(@RequestParam("jwt") String jwt, HttpSession session, Model model) {
        try {
            var data = magicJwt.parse(jwt); // verifică semnătură + expirare
            var email = data.email();
            var roundId = data.roundId();

            var user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            var cid = new CandidateId(user.getIdUser(), roundId);
            Candidate cand = candidateRepo.findById(cid)
                    .orElseThrow(() -> new RuntimeException("User not a candidate in this round"));

            if (cand.isTokenUsed()) {
                model.addAttribute("error", "Link already used");
                return "invalid";
            }

            // ONE-TIME per (user, round)
            cand.setTokenUsed(true);
            candidateRepo.save(cand);

            // Mini "login" de sesiune (dacă vrei alt mecanism, adaptezi)
            session.setAttribute("userEmail", email);
            model.addAttribute("email", email);
            model.addAttribute("roundId", roundId);
            model.addAttribute("time", LocalDateTime.now().toString());

            // Returnează o pagină simplă sau redirect către UI-ul tău SPA
            // return "redirect:/ui/round/" + roundId;
            return "welcome";

        } catch (JwtException e) {
            model.addAttribute("error", "Invalid or expired link");
            return "invalid";
        }
    }
}
