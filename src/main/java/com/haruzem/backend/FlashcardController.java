package com.haruzem.backend;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class FlashcardController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/generate-flashcards")
    public String generateFlashcards(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "count", defaultValue = "8") int count,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] pdfBytes = file.getBytes();

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            Content content = Content.fromParts(
                    Part.fromText(
                            "Bu PDF içeriğinden " + langName + " dilinde, " + count + " adet bilgi kartı (flashcard) oluştur. " +
                                    "Her kart, önemli bir kavramı (ön yüz) ve kısa açıklamasını (arka yüz) içersin. Kartların tamamı " + langName + " dilinde olsun. " +
                                    "Sadece geçerli bir JSON dizisi döndür, başka hiçbir açıklama ekleme. " +
                                    "Format tam olarak şöyle olsun: " +
                                    "[{\"term\": \"Kavram adı\", \"definition\": \"Kısa açıklama\"}]"
                    ),
                    Part.fromBytes(pdfBytes, "application/pdf")
            );

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-flash-lite-latest",
                    content,
                    config
            );

            return response.text();
        } catch (Exception e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }
}