package com.haruzem.backend;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/chat-pdf")
    public String chatWithPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("question") String question,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] pdfBytes = file.getBytes();

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            String instruction = "Bu PDF içeriğine dayanarak, öğrencinin sorusunu " + langName + " dilinde ve anlaşılır şekilde cevapla. Soru: " + question +
                    " Cevabını tamamen " + langName + " dilinde ver. Sade düz metin olarak yaz; Markdown biçimlendirmesi (**, *, #, - gibi işaretler) veya LaTeX matematik gösterimi ($, \\times, \\frac gibi) kullanma.";

            Content content = Content.fromParts(
                    Part.fromText(instruction),
                    Part.fromBytes(pdfBytes, "application/pdf")
            );

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3.5-flash",
                    content,
                    null
            );

            return response.text();
        } catch (Exception e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }
}