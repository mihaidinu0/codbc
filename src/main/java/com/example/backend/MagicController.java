package com.example.backend.controller;

import com.example.backend.service.MagicService;
import com.example.backend.service.MagicService.Outcome;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class MagicController {

    private final MagicService magicService;

    @GetMapping("/magic/verify")
    public ResponseEntity<Void> verify(@RequestParam("jwt") String jwt, HttpServletResponse resp) {
        Outcome outcome = magicService.verify(jwt);

        if (outcome.getCookie() != null) {
            resp.addCookie(outcome.getCookie());
        }

        return ResponseEntity.status(303) // See Other
                .header(HttpHeaders.LOCATION, outcome.getRedirect().toString())
                .build();
    }
}
