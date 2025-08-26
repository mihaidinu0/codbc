package com.example.backend.service;

import com.example.backend.dto.CreateRoundRequest;
import com.example.backend.dto.CreateRoundResponse;
import com.example.backend.model.*;
import com.example.backend.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepo;
    private final UserRepository userRepo;
    private final CandidateRepository candidateRepo;
    private final EmailService emailService;

    @Transactional
    public CreateRoundResponse createRound(CreateRoundRequest dto) {
        // 1) creator
        MyUser creator = userRepo.findByEmail(dto.getCreatorEmail())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found: " + dto.getCreatorEmail()));

        // 2) runda
        Round round = new Round();
        round.setTitle(dto.getTitle());
        round.setLevel(dto.getLevel());
        round.setCreator(creator);
        round = roundRepo.save(round);

        // 3) normalizează emailurile + ia userii existenți
        var uniqEmails = dto.getCandidateEmails().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var users = userRepo.findByEmailIn(uniqEmails);
        var found = users.stream().map(MyUser::getEmail).map(String::toLowerCase).collect(Collectors.toSet());
        var notFound = uniqEmails.stream().filter(e -> !found.contains(e.toLowerCase())).toList();

        // 4) creează legăturile candidat ↔ rundă
        List<Candidate> links = new ArrayList<>();
        for (MyUser u : users) {
            Candidate c = new Candidate();
            c.setIdCandidate(new CandidateId(u.getIdUser(), round.getIdRound())); // (userId, roundId)
            c.setCandidate(u);
            c.setRound(round);
            c.setStatus("INVITED");
            c.setTokenUsed(false);
            links.add(c);
        }
        candidateRepo.saveAll(links);

        // 5) trimite câte un link per candidat (JWT cu sub=email, rid=roundId, exp)
        for (MyUser u : users) {
            emailService.sendMagicLink(u.getEmail(), round.getIdRound());
        }

        // 6) răspuns
        var resp = new CreateRoundResponse();
        resp.setIdRound(round.getIdRound());
        resp.setInvitedCount(users.size());
        resp.setNotFoundEmails(notFound);
        return resp;
    }
}
