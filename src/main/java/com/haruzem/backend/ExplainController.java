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
public class ExplainController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/explain")
    public String explain(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "basit") String mode,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] pdfBytes = file.getBytes();

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            String instruction;
            switch (mode) {
                case "ornek":
                    instruction = "Bu PDF içeriğini " + langName + " dilinde anlat, ama her önemli kavram için günlük hayattan somut bir örnek ver, böylece konu daha anlaşılır olsun.";
                    break;
                case "adimadim":
                    instruction = "Bu PDF içeriğini " + langName + " dilinde, numaralandırılmış adımlar halinde, adım adım ve mantıksal sırayla anlat. Her adımı yeni bir satırda yaz, adımları birbirine bitişik yazma; her adımdan sonra mutlaka satır sonu (yeni paragraf) kullan.";
                    break;
                case "kafamkaristi":
                    instruction = "Bu PDF içeriğini, konuyu ilk kez duyan ve kafası karışmış bir öğrenciye, sıfırdan, çok sabırlı ve yavaş yavaş, en temel kavramlardan başlayarak " + langName + " dilinde anlat.";
                    break;
                default:
                    instruction = "Bu PDF içeriğini " + langName + " dilinde, karmaşık akademik dili sadeleştirerek, mümkün olduğunca basit ve anlaşılır kelimelerle anlat.";
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