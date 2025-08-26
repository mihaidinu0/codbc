package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundService roundService;

    @PostMapping("/create")
    public ResponseEntity<CreateRoundResponse> create(@RequestBody CreateRoundRequest dto) {
        var resp = roundService.createRound(dto);
        return ResponseEntity.ok(resp);
    }
}
