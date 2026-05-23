# 🧠 AI-Powered Message-Based CRM & Automation Platform

> Mesaj tabanlı müşteri iletişimini merkezi olarak yöneten, AI destekli temsilci asistanı özellikleri sunan backend sistemi.

---

## 📌 Proje Hakkında

Bu proje, küçük ve orta ölçekli işletmelerin müşteri iletişimini sistematik hale getirmek amacıyla geliştirilmiş bir **CRM backend sistemidir**. WhatsApp veya benzeri kanallar üzerinden gelen müşteri mesajlarını merkezi bir yapıda yönetmeyi, temsilci iş akışını kolaylaştırmayı ve AI desteğiyle operasyonel verimliliği artırmayı hedefler.

Proje, gereksiz teknoloji eklemekten kaçınarak **fazlara bölünmüş, her fazı tek başına çalışan ve gösterilebilir** bir yapıda geliştirilmiştir.

---

## ✨ Özellikler

### 🔐 Kimlik Doğrulama & Yetkilendirme
- JWT tabanlı kimlik doğrulama
- Rol bazlı yetkilendirme (ADMIN / AGENT)
- Güvenli şifre yönetimi (BCrypt)

### 👥 Müşteri Yönetimi (CRM)
- Müşteri oluşturma, güncelleme, listeleme, soft delete
- E-posta ve telefon üzerinden tekil kayıt kontrolü
- Durum yönetimi (ACTIVE / PASSIVE / BLOCKED)
- Etiket ekleme / çıkarma

### 💬 Konuşma & Mesaj Yönetimi
- Müşteri bazlı konuşma oluşturma
- Konuşma durumu yönetimi (OPEN / PENDING / CLOSED / ARCHIVED)
- Temsilci atama sistemi
- Mesaj yönü ve gönderici tipi validasyonu (INBOUND/OUTBOUND × CUSTOMER/AGENT/SYSTEM)
- `lastMessageAt` otomatik güncelleme

### 📝 Not Sistemi
- Müşteri ve konuşma bazlı iç not ekleme
- Not sahibi kontrolü (sadece oluşturan düzenleyebilir/silebilir)

### 📊 Dashboard & Operasyonel Görünüm
- Açık / bekleyen / atanmamış konuşma sayıları
- Bugünkü müşteri, mesaj ve kapanan konuşma metrikleri
- Bana atanmış konuşmalar
- Cevap bekleyen konuşmalar (son mesajı INBOUND olan)
- Konuşma durum dağılımı
- Etiket bazlı müşteri dağılımı

### 🤖 AI Entegrasyonu (OpenAI)
- **Cevap Önerisi** — Son mesajlar ve konuşma geçmişine göre temsilciye cevap önerisi
- **Konuşma Özeti** — Uzun konuşmaları otomatik özetleme
- **Sınıflandırma** — Niyet, duygu ve öncelik analizi
- **Etiket Önerisi** — Konuşmaya uygun etiket önerileri
- Tüm AI çıktıları `ai_result` tablosunda saklanır, müşteriye otomatik gönderilmez
- Model, timeout, token limiti ve prompt versiyonu config üzerinden yönetilebilir

---

## 🏗️ Mimari

```
Modüler Monolith — Layered Architecture
```

```
HTTP Request
    │
    ▼
JWT Filter (Spring Security)
    │
    ▼
Controller (@RestController)
    │
    ▼  Request DTO
Service (@Service + @Transactional)
    │
    ├──▶ Repository (Spring Data JPA)
    │         │
    │         ▼
    │     PostgreSQL
    │
    └──▶ Mapper (MapStruct)
              │
              ▼
          Response DTO
              │
              ▼
        HTTP Response
```

### Paket Yapısı

```
com.yunus
├── auth              # Kimlik doğrulama, kullanıcı yönetimi
├── common            # BaseEntity, PageResponse
├── config            # SecurityConfig, SwaggerConfig, DataInitializer
├── security          # JWT, UserPrincipal, CustomUserDetailsService
├── exception         # GlobalExceptionHandler, BusinessException, ErrorType
├── enums             # CustomerStatus, ConversationStatus, MessageDirection, SenderType
├── tag               # Tag CRUD
├── customer          # Müşteri yönetimi
├── conversation      # Konuşma yönetimi
├── message           # Mesaj yönetimi
├── note              # Not sistemi
├── dashboard         # Metrikler ve operasyonel görünüm
└── ai                # AI entegrasyon katmanı
    ├── dto
    ├── entity
    ├── enums
    ├── repository
    ├── mapper
    ├── controller
    ├── service
    ├── provider
    └── config
```

---

## 🛠️ Teknoloji Stack

| Katman | Teknoloji |
|---|---|
| Dil | Java 17 |
| Framework | Spring Boot |
| Güvenlik | Spring Security + JWT |
| Veritabanı | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Mapper | MapStruct |
| AI | OpenAI API (gpt-4o-mini) |
| Containerization | Docker + Docker Compose |
| API Dokümantasyonu | Swagger / OpenAPI |
| Build | Maven |
| Test | JUnit 5 + Mockito |

---

## 🗄️ Veri Modeli

```
User ──────────────────────────────────────────┐
 │ (assignedTo)                                 │ (createdBy)
 ▼                                              ▼
Conversation ◀──── Customer ────▶ Tag        Note
 │                    │
 ▼                    └──────────▶ Tag (ManyToMany)
Message
 │
 └── senderUser (User, nullable)

AiResult ──▶ Conversation
         ──▶ Customer (nullable)
         ──▶ User (requestedBy)
```

### Temel Enum'lar

```java
CustomerStatus:      ACTIVE | PASSIVE | BLOCKED
ConversationStatus:  OPEN | PENDING | CLOSED | ARCHIVED
MessageDirection:    INBOUND | OUTBOUND
SenderType:          CUSTOMER | AGENT | SYSTEM
AiResultType:        REPLY_SUGGESTION | CONVERSATION_SUMMARY | CONVERSATION_CLASSIFICATION | TAG_SUGGESTION
AiResultStatus:      SUCCESS | FAILED
```

---

## 🚀 Kurulum

### Gereksinimler

- Java 17+
- Docker & Docker Compose
- OpenAI API Key (AI özellikleri için)

### 1. Projeyi Klonla

```bash
git clone https://github.com/kullanici-adi/crm-project.git
cd crm-project
```

### 2. Ortam Değişkenlerini Ayarla

`application.yml` veya `.env` dosyasında aşağıdaki değişkenleri düzenle:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/crm_db
    username: postgres
    password: your_password

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000

ai:
  openai:
    api-key: your_openai_api_key
    model: gpt-4o-mini
    max-output-tokens: 500
    timeout-seconds: 30
```

### 3. Docker ile Çalıştır

```bash
docker-compose up -d
```

### 4. Uygulamayı Başlat

```bash
./mvnw spring-boot:run
```

Uygulama ayağa kalktığında `DataInitializer` otomatik olarak çalışır ve şu verileri oluşturur:

- `ADMIN` ve `AGENT` rolleri
- Varsayılan admin kullanıcısı:
  - Email: `admin@crm.com`
  - Şifre: `admin123`

---

## 📡 API Endpoint'leri

### Auth
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/auth/register` | Yeni kullanıcı kaydı (AGENT rolü) |
| POST | `/api/auth/login` | Giriş ve JWT token alma |

### Tag
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/tags` | Tag oluştur |
| GET | `/api/tags` | Tüm tag'leri listele (paginated) |
| GET | `/api/tags/{id}` | Tag detayı |
| PUT | `/api/tags/{id}` | Tag güncelle |
| DELETE | `/api/tags/{id}` | Tag sil (soft delete) |

### Customer
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/customers` | Müşteri oluştur |
| GET | `/api/customers` | Filtreli müşteri listesi |
| GET | `/api/customers/{id}` | Müşteri detayı |
| PUT | `/api/customers/{id}` | Müşteri güncelle |
| DELETE | `/api/customers/{id}` | Müşteri sil (soft delete) |
| POST | `/api/customers/{customerId}/tags/{tagId}` | Müşteriye tag ekle |
| DELETE | `/api/customers/{customerId}/tags/{tagId}` | Müşteriden tag çıkar |

### Conversation
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/customers/{customerId}/conversations` | Konuşma oluştur |
| GET | `/api/customers/{customerId}/conversations` | Müşteri konuşmaları |
| GET | `/api/conversations` | Filtreli konuşma listesi |
| GET | `/api/conversations/{id}` | Konuşma detayı |
| GET | `/api/conversations/my` | Bana atanmış konuşmalar |
| GET | `/api/conversations/unassigned` | Atanmamış konuşmalar |
| GET | `/api/conversations/waiting` | Cevap bekleyen konuşmalar |
| PATCH | `/api/conversations/{id}/status` | Durum güncelle |
| PATCH | `/api/conversations/{id}/assign` | Temsilci ata |
| DELETE | `/api/conversations/{id}` | Konuşma sil |

### Message
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/conversations/{conversationId}/messages` | Mesaj gönder |
| GET | `/api/conversations/{conversationId}/messages` | Mesajları listele |

### Note
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/customers/{customerId}/notes` | Müşteri notu ekle |
| GET | `/api/customers/{customerId}/notes` | Müşteri notları |
| POST | `/api/conversations/{conversationId}/notes` | Konuşma notu ekle |
| GET | `/api/conversations/{conversationId}/notes` | Konuşma notları |
| GET | `/api/notes/{id}` | Not detayı |
| PUT | `/api/notes/{id}` | Not güncelle |
| DELETE | `/api/notes/{id}` | Not sil |

### Dashboard
| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/dashboard/summary` | Genel metrikler |
| GET | `/api/dashboard/conversation-status-distribution` | Durum dağılımı |
| GET | `/api/dashboard/customer-tag-distribution` | Etiket dağılımı |

### AI
| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/conversations/{conversationId}/ai/reply-suggestion` | Cevap önerisi |
| POST | `/api/conversations/{conversationId}/ai/summary` | Konuşma özeti |
| POST | `/api/conversations/{conversationId}/ai/classify` | Sınıflandırma |
| POST | `/api/conversations/{conversationId}/ai/tag-suggestion` | Etiket önerisi |
| GET | `/api/conversations/{conversationId}/ai/results` | AI sonuçlarını listele |

---

## 📖 API Dokümantasyonu

Uygulama çalışırken Swagger UI'a şu adresten erişebilirsiniz:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 🔄 Geliştirme Fazları

### ✅ Faz 1 — Core Backend / CRM Temeli
JWT auth, rol yönetimi, müşteri/konuşma/mesaj/not/tag CRUD, soft delete, global exception handling, pagination, Swagger, Docker.

### ✅ Faz 2 — Operasyonel Kullanım & Dashboard
Gelişmiş filtreleme (Specification pattern), bana atanmış / atanmamış / cevap bekleyen konuşmalar, dashboard metrikleri, tag ve müşteri dağılım analizleri.

### ✅ Faz 3 — AI Entegrasyonu
OpenAI tabanlı cevap önerisi, konuşma özeti, sınıflandırma ve etiket önerisi. AI çıktıları `ai_result` tablosunda saklanır, temsilci kontrolü korunur.

### 🔜 Faz 4 — Otomasyon Katmanı *(Planlanan)*
Event bazlı trigger-action sistemi, webhook entegrasyonu, otomatik görev oluşturma.

### 🔜 Faz 5 — Gelişmiş Sistemleşme *(Planlanan)*
WhatsApp Business API, WebSocket/SSE, CI/CD pipeline, multi-tenant yapı, Redis cache.

---

## 🧪 Test

```bash
./mvnw test
```

Projede şu test kategorileri bulunmaktadır:
- Service unit testleri (JUnit 5 + Mockito)
- Controller integration testleri
- Security testleri
- Validation testleri
- AI output doğrulama testleri

---

## 🏛️ Mimari Kararlar

**Neden modüler monolith?**
İlk aşamada microservice gereksiz karmaşıklık ekler. Domain sınırları netleşince gerekirse ayrıştırılabilir.

**Neden JWT içinde role taşınmıyor?**
Token'ın geçerlilik süresi boyunca rol değişikliği anında yansımazdı. Her request'te DB'den yükleme tercih edildi.

**Neden AI çıktısı ayrı tabloda?**
Gerçek müşteri mesajı değil, temsilci için öneri. `Message` tablosunu kirletmemek ve AI geçmişini ayrıca takip etmek için `ai_result` entity'si tercih edildi.

**Neden Specification pattern?**
Dinamik filtre parametreleri için derived query yazmak maintainable değil. `JpaSpecificationExecutor` ile tip güvenli, genişletilebilir filtre yapısı kuruldu.

**Neden PageResponse wrapper'ı?**
Spring `Page<T>` çok fazla metadata döner. Frontend için sade, tutarlı bir response yapısı için `PageResponse<T>` tercih edildi.

---

## 👤 Geliştirici

**Yunus Emre**
- Backend Developer
- Java | Spring Boot | PostgreSQL | Docker | CI/CD | Redis | Spring Ai | jUnit/Mockito

---

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.
