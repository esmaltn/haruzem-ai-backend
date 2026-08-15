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
public class SectionedController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/sectioned-explain")
    public String sectionedExplain(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] pdfBytes = file.getBytes();

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            Content content = Content.fromParts(
                    Part.fromText(
                            "Bu PDF içeriğini mantıksal bölümlere/konulara ayır. Her bölüm için: " +
                                    "1) bölümün başlığı, 2) bölümün içeriğinin öğrenme güçlüğü yaşayan bir öğrenci için " +
                                    "basitleştirilmiş, anlaşılır bir anlatımı, 3) o bölümün sonunda yer alacak kısa bir özet kutusu metni üret. " +
                                    "Tüm metinler " + langName + " dilinde olsun. " +
                                    "Sadece geçerli bir JSON dizisi döndür, başka açıklama ekleme. " +
                                    "Format tam olarak şöyle olsun: " +
                                    "[{\"title\": \"Bölüm başlığı\", \"content\": \"Basitleştirilmiş anlatım\", \"summaryBox\": \"Kısa özet\"}]. " +
                                    "Cevabın içindeki tüm metinlerde Markdown biçimlendirmesi (**, *, #, - gibi işaretler) veya " +
                                    "LaTeX matematik gösterimi ($, \\\\times, \\\\frac gibi) kullanma. " +
                                    "Türkçe dilinde yazıyorsan, Türkçe karakterleri (ç, ğ, ı, İ, ö, ş, ü) mutlaka doğru ve eksiksiz kullan."
                    ),
                    Part.fromBytes(pdfBytes, "application/pdf")
            );

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3.5-flash",
                    content,
                    config
            );

            return response.text();
        } catch (Exception e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }
}