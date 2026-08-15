package com.haruzem.backend;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class GeminiController {

    @Value("${gemini.api.key}")
    private String apiKey;

    @GetMapping("/api/gemini-test")
    public String testGemini() {
        try {
            Client client = Client.builder().apiKey(apiKey).build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-flash-lite-latest",
                    "Merhaba! Sadece 'Bağlantı başarılı' yaz.",
                    null
            );

            return response.text();
        } catch (Exception e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }
}