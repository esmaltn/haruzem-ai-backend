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
public class PdfController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/summarize-pdf")
    public String summarizePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "level", defaultValue = "orta") String level,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] pdfBytes = file.getBytes();

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            String instruction;
            switch (level) {
                case "kisa":
                    instruction = "Bu PDF dosyasını " + langName + " dilinde, en fazla 2-3 cümlelik çok kısa bir özetle.";
                    break;
                case "detayli":
                    instruction = "Bu PDF dosyasını " + langName + " dilinde, tüm önemli noktaları kapsayan ayrıntılı ve uzun bir özetle. Gerekirse alt başlıklar kullan.";
                    break;
                default:
                    instruction = "Bu PDF dosyasını " + langName + " dilinde, öğrencinin kolayca anlayabileceği orta uzunlukta bir özetle (1-2 paragraf).";
            }

            instruction += " Cevabını tamamen " + langName + " dilinde ver. Sade düz metin olarak yaz; Markdown biçimlendirmesi (**, *, #, - gibi işaretler) veya LaTeX matematik gösterimi ($, \\times, \\frac gibi) kullanma.";

            Content content = Content.fromParts(
                    Part.fromText(instruction),
                    Part.fromBytes(pdfBytes, "application/pdf")
            );

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-flash-lite-latest",
                    content,
                    null
            );

            return response.text();
        } catch (Exception e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }
}