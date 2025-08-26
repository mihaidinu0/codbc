package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateRoundResponse {
    private Long idRound;
    private int invitedCount;
    private List<String> notFoundEmails; // useri inexistenți
}
