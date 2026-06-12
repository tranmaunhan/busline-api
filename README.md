# SaigonST BusLine API

Backend Spring Boot cho he thong dat ve xe khach. Du an cung cap API dang ky, dang nhap JWT, tra cuu diem don tra, tim chuyen xe, xem chi tiet/sơ do ghe va tao booking tam giu ghe cho nguoi dung da dang nhap.

## Cong nghe

- Java 21
- Spring Boot 4
- Spring Web MVC, Spring Security, Spring Data JPA, Validation
- PostgreSQL
- JWT voi `jjwt`
- Swagger/OpenAPI voi `springdoc-openapi`
- Maven Wrapper
- Docker

## Cau truc chinh

```text
src/main/java/com/busline/tranmaunhan
├── config        # Security, CORS, Jackson, OpenAPI
├── controller    # REST API endpoints
├── dto           # Request/response models
├── entity        # JPA entities map voi database
├── exception     # Xu ly loi tap trung
├── repository    # Spring Data JPA repositories
├── security      # JWT filter/provider va UserDetails
└── service       # Business logic
```

## Bien moi truong

Ung dung doc cau hinh tu bien moi truong. Tao bien tuong ung tren may local, Docker hoac CI/CD:

```env
DB_URL=jdbc:postgresql://localhost:5432/busline
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=your-very-long-secret-key
JWT_EXPIRATION_MS=86400000
APP_DEFAULT_ROLE=Customer
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://aihost.io.vn
```

Mac dinh ung dung dung profile `prod` trong `src/main/resources/application.properties`. Co the doi profile khi chay:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
```

## Chay local

Yeu cau cai Java 21. Maven co the dung qua wrapper co san trong repo.

```powershell
.\mvnw.cmd clean spring-boot:run
```

Neu dung terminal Linux/macOS:

```bash
./mvnw clean spring-boot:run
```

API mac dinh chay tai:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Build va test

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

File jar sau khi build nam trong thu muc `target/`.

## Chay bang Docker

Build jar truoc:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Build image:

```powershell
docker build -t busline-api:local .
```

Chay container:

```powershell
docker run --rm -p 8080:8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/busline" `
  -e DB_USERNAME="postgres" `
  -e DB_PASSWORD="postgres" `
  -e JWT_SECRET="your-very-long-secret-key" `
  busline-api:local
```

## Endpoint chinh

| Method | Endpoint | Auth | Mo ta |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Khong | Dang ky tai khoan va nhan JWT |
| `POST` | `/api/auth/login` | Khong | Dang nhap va nhan JWT |
| `GET` | `/api/auth/me` | Bearer token | Lay thong tin user hien tai |
| `GET` | `/api/locations` | Khong | Lay danh sach diem don/tra |
| `GET` | `/api/trips/search` | Khong | Tim chuyen theo diem don, diem tra, ngay di |
| `GET` | `/api/trips/{tripId}/details` | Khong | Lay chi tiet chuyen xe |
| `GET` | `/api/trips/{tripId}/seat-map` | Khong | Lay so do ghe cua chuyen xe |
| `POST` | `/api/bookings` | Bearer token | Tao booking va lock ghe |

## Vi du request

Dang ky:

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "customer01",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "customer01@example.com",
  "phone": "0900000000"
}
```

Dang nhap:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "customer01",
  "password": "123456"
}
```

Tim chuyen:

```http
GET /api/trips/search?pickupLocationId=1&dropoffLocationId=2&departureDate=2026-06-05
```

Tao booking:

```http
POST /api/bookings
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "tripId": 1,
  "tripSeatIds": [10, 11],
  "pickupLocationId": 1,
  "dropoffLocationId": 2
}
```

## Ghi chu du lieu

- Database PostgreSQL can co san cac bang tuong ung voi entity trong `src/main/java/.../entity`.
- Role mac dinh khi dang ky lay theo `APP_DEFAULT_ROLE`, gia tri mac dinh la `Customer`.
- Script bo sung gia chang nam tai `src/main/resources/db/manual/route-segment-prices.sql`.
- Booking hien tai tao trang thai `PENDING` va doi ghe sang trang thai `LOCKED`.

## CI/CD

Workflow `.github/workflows/docker-image.yml` build jar, build/push Docker image len Docker Hub va deploy len VPS khi push vao branch `main`. Can cau hinh cac GitHub Secrets sau:

```text
DOCKER_USERNAME
DOCKER_PASSWORD
SERVER_HOST
SERVER_USER
SERVER_SSH_KEY
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

## Cac phan da don dep

- Xoa log chay thu va file output tam.
- Xoa file `.class` sinh ra o root.
- Xoa script JDBC kiem tra DB hard-code thong tin ket noi.
- Xoa thu muc build `target/`, cau hinh IDE local va metadata nang cap Java khong dung trong runtime.
- Bo sung `.gitignore` de tranh commit lai cac artifact nay.
