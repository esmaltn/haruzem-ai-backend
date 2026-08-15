# HARUZEM AI — Backend

Yapay zekâ destekli, erişilebilir eğitim platformu **HARUZEM AI**'nin backend uygulaması.

Harran Üniversitesi Uzaktan Eğitim Merkezi (HARUZEM) öğrencileri için PDF ve video ders materyallerini analiz edip; özetleme, soru-cevap, bölümlere ayrılmış anlatım, quiz ve bilgi kartı (flashcard) üretimi gibi işlemleri Google Gemini API üzerinden gerçekleştirir.

🔗 **Frontend reposu:** [haruzem-ai](https://github.com/esmaltn/haruzem-ai)

## Kullanılan Teknolojiler

- **Spring Boot** (Java)
- **Google Gemini API** (`google-genai` istemci kütüphanesi) — PDF/video analizi
- **Railway** — deployment

## API Uç Noktaları (Endpoints)

| Endpoint | Açıklama |
|---|---|
| `POST /api/summarize-pdf` | PDF'i özetler (kısa/orta/detaylı) |
| `POST /api/chat-pdf` | PDF içeriğine dayalı soru-cevap |
| `POST /api/sectioned-explain` | İçeriği bölümlere ayırıp her biri için özet üretir |
| `POST /api/explain` | Basitleştirilmiş / örneklerle / adım adım anlatım |
| `POST /api/generate-quiz` | PDF'ten çoktan seçmeli quiz üretir |
| `POST /api/generate-flashcards` | PDF'ten bilgi kartları (flashcard) üretir |
| `POST /api/video-analyze` | Video transkript, altyazı, not, kavram ya da özet çıkarır |

## Kurulum (Yerel Geliştirme)

`src/main/resources/application.properties` dosyası oluşturup Gemini API anahtarınızı ekleyin:

```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

> Bu dosya `.gitignore` içinde tutulur ve repoya yüklenmez — API anahtarınızı asla commit etmeyin.

Çalıştırmak için:

```bash
./mvnw spring-boot:run
```

Uygulama varsayılan olarak `http://localhost:8080` adresinde çalışır.

## Canlı Ortam (Deployment) Notları

Railway gibi bir platformda deploy ederken şu ortam değişkenlerini tanımlamanız gerekir:

```
GEMINI_API_KEY=your_gemini_api_key
SERVER_SERVLET_ENCODING_CHARSET=UTF-8
SERVER_SERVLET_ENCODING_ENABLED=true
SERVER_SERVLET_ENCODING_FORCE=true
```

Büyük dosya yüklemelerinde hata almamak için (varsayılan limit 1MB'tır):

```
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=50MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=50MB
```

## İlgili Repo

Frontend (React) için: [haruzem-ai](https://github.com/esmaltn/haruzem-ai)
## Geliştirici

Bu proje **Esma Altun** tarafından geliştirilmiştir.

- GitHub: [@esmaltn](https://github.com/esmaltn)
- Geliştirme süreci: Temmuz - Ağustos 2026
- Harran Üniversitesi Uzaktan Eğitim Merkezi (HARUZEM) için staj projesi

Detaylı geliştirme geçmişi için bu reponun [commit geçmişine](../../commits) bakabilirsiniz.

## Lisans

Bu proje için [LICENSE](./LICENSE.md) dosyasına bakınız. Tüm hakları saklıdır.