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
public class MindMapController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/mindmap")
    public String generateMindMap(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] pdfBytes = file.getBytes();

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            Content content = Content.fromParts(
                    Part.fromText(
                            "Bu PDF içeriğini bir kavram haritası (mind map) olarak, hiyerarşik bir ağaç " +
                                    "yapısında çıkar. Ana konuyu kök (root) olarak, alt konuları ve onların da " +
                                    "alt kırılımlarını dallar olarak organize et. En fazla 3 seviye derinlik kullan " +
                                    "ve her seviyede en fazla 5 dal olsun. Başlıklar kısa ve öz olsun (en fazla " +
                                    "5-6 kelime). Tüm metinler " + langName + " dilinde olsun. " +
                                    "Sadece geçerli bir JSON nesnesi döndür, başka açıklama ekleme. " +
                                    "Format tam olarak şöyle olsun: " +
                                    "{\"title\": \"Ana Konu\", \"children\": [{\"title\": \"Alt Konu\", " +
                                    "\"children\": [{\"title\": \"Detay\", \"children\": []}]}]}. " +
                                    "Dalı olmayan yapraklar için children alanını boş dizi olarak bırak. " +
                                    "Markdown biçimlendirmesi (**, *, #, - gibi işaretler) kullanma. " +
                                    "Türkçe dilinde yazıyorsan, Türkçe karakterleri (ç, ğ, ı, İ, ö, ş, ü) " +
                                    "mutlaka doğru ve eksiksiz kullan."
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