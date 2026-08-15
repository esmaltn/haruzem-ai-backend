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
public class VideoController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String languageName(String lang) {
        switch (lang) {
            case "en": return "İngilizce";
            case "ar": return "Arapça";
            default: return "Türkçe";
        }
    }

    @PostMapping("/api/video-analyze")
    public String analyzeVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "transcript") String mode,
            @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        try {
            byte[] videoBytes = file.getBytes();
            String mimeType = file.getContentType();
            if (mimeType == null) {
                mimeType = "video/mp4";
            }

            Client client = Client.builder().apiKey(apiKey).build();

            String langName = languageName(lang);
            String instruction;
            boolean skipPlainTextRule = false;

            switch (mode) {
                case "notlar":
                    instruction = "Bu videonun konuşmalarına dayanarak " + langName + " dilinde, düzenli bir ders notu oluştur. Başlıklar ve maddeler halinde yaz. " +
                            "Türkçe dilinde yazıyorsan, Türkçe karakterleri (ç, ğ, ı, İ, ö, ş, ü) mutlaka doğru ve eksiksiz kullan.";
                    break;
                case "kavramlar":
                    instruction = "Bu videodaki anahtar kavramları ve önemli noktaları " + langName + " dilinde, madde madde listele.";
                    break;
                case "ozet":
                    instruction = "Bu videoyu " + langName + " dilinde, öğrencinin kolayca anlayabileceği kısa bir paragrafla özetle.";
                    break;
                case "betimleme":
                    instruction = "Bu video, görme engelli bir öğrenci için sesli betimleme metni olarak kullanılacak. " +
                            "Videodaki konuşmaları aktarmanın yanı sıra, ekranda görünen ve konuşmada belirtilmeyen görsel unsurları " +
                            "(slaytlar, grafikler, yazılar, diyagramlar, hareketler, jestler) da ayrıntılı şekilde " + langName + " dilinde anlat. " +
                            "Öğrenci videoyu göremiyormuş gibi düşünerek, görsel içeriği tarif et.";
                    break;
                case "altyazi":
                    instruction = "Bu videodaki konuşmaları, SRT altyazı formatında " + langName + " dilinde olarak zaman damgalarıyla birlikte yaz. " +
                            "Format tam olarak şöyle olsun:\n" +
                            "1\n00:00:00,000 --> 00:00:03,000\nİlk konuşma metni\n\n" +
                            "2\n00:00:03,000 --> 00:00:06,000\nİkinci konuşma metni\n\n" +
                            "Sadece SRT formatında çıktı ver, başka açıklama ekleme.";
                    skipPlainTextRule = true;
                    break;
                default:
                    instruction = "Bu videodaki tüm konuşmaları " + langName + " dilinde eksiksiz yazıya dök (transkript). Konuşmacı değişimlerini fark edersen belirt.";
            }

            if (!skipPlainTextRule) {
                instruction += " Cevabını tamamen " + langName + " dilinde ver. Sade düz metin olarak yaz; Markdown biçimlendirmesi (**, *, #, - gibi işaretler) veya LaTeX matematik gösterimi ($, \\times, \\frac gibi) kullanma.";
            }

            Content content = Content.fromParts(
                    Part.fromText(instruction),
                    Part.fromBytes(videoBytes, mimeType)
            );

            String modelToUse = mode.equals("altyazi") ? "gemini-3.5-flash" : "gemini-flash-lite-latest";

            GenerateContentResponse response = client.models.generateContent(
                    modelToUse,
                    content,
                    null
            );
            return response.text();
        } catch (Exception e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }
}