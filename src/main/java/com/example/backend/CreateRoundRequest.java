package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateRoundRequest {
    private String title;
    private String level;
    private String creatorEmail;          // identificăm organizatorul prin email
    private List<String> candidateEmails; // email-urile candidaților
}
