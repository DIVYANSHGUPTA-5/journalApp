# JournalApp

A secure Spring Boot REST API for managing journal entries, with JWT authentication, Google OAuth2 login, weather integration, Redis caching and e-mail notifications.

JournalApp is a **local-only application**: it runs entirely on your own machine, using MongoDB and Redis on `localhost`. There is no cloud-deployment configuration in this repository.

---

## 🚀 Features

- User authentication (Spring Security, JWT bearer tokens, Google OAuth2 login)
- Journal CRUD operations
- MongoDB integration
- Redis caching for faster responses
- Weather API integration
- Email notification support
- Role-based access control
- Secure password encryption (BCrypt)
- Sentiment analysis support for journal entries
- Scheduled tasks using Spring Scheduler
- Configuration through environment variables / a git-ignored local file (no secrets in the repository)
- Unit testing with JUnit and Mockito

---

## 🛠 Tech Stack

- Java 21, Spring Boot 3.2.5
- Spring Security
- MongoDB (local, `localhost:27017`)
- Redis (local, `localhost:6379`)
- Maven (wrapper included)
- springdoc-openapi (Swagger UI)
- Lombok
- JUnit & Mockito

---

## 🏗 Project Architecture

The project follows a **layered architecture**.

Client (Postman / Frontend)  
↓  
Controller Layer  
↓  
Service Layer (Business Logic)  
↓  
Repository Layer  
↓  
MongoDB Database

Additional components:

Weather API → RestTemplate  
Redis → caching layer  
Scheduler → sentiment analysis  
EmailService → send notifications  
Spring Security → authentication & authorization

---

## ▶ Running the project locally

### 1. Prerequisites (macOS / Homebrew)

| Requirement | Install / start |
|---|---|
| JDK 21 | `brew install openjdk@21`, then `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| MongoDB 7 on `localhost:27017` | `brew tap mongodb/brew && brew install mongodb-community@7.0 && brew services start mongodb-community@7.0` |
| Redis on `localhost:6379` | `brew install redis && brew services start redis` |

### 2. Local configuration

The Spring profile is `dev` (set in `application.yml`). Non-secret settings (MongoDB URI, Redis, SMTP host, ...) are in `src/main/resources/application-dev.yml`.

Secrets are **not** in the repository. They live in a git-ignored file, `config/application-dev.yml`, which Spring Boot loads automatically when the app is started from the project root (IntelliJ and `./mvnw spring-boot:run` both do this). Create it from the template:

```bash
cp config/application-dev.yml.example config/application-dev.yml
# then edit it. A JWT key can be generated with:  openssl rand -base64 32
```

Every setting can alternatively be supplied as an environment variable (environment variables win over the file):

| Setting | Environment variable | Required? | Purpose |
|---|---|---|---|
| `jwt.secret` | `JWT_SECRET` | **Yes** (32+ characters) | Signs login tokens. The app refuses to start without it. |
| `weather.api.key` | `WEATHER_API_KEY` | No | weatherstack key for the weather greeting |
| `spring.mail.username` / `spring.mail.password` | `MAIL_USERNAME` / `MAIL_PASSWORD` | No | Gmail address + App Password for e-mails |
| `spring.security.oauth2.client.registration.google.client-id` / `client-secret` | `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | No | "Login with Google" |
| `spring.data.mongodb.uri` / `spring.data.mongodb.database` | `SPRING_DATA_MONGODB_URI` / `SPRING_DATA_MONGODB_DATABASE` | No | Use a different MongoDB than `localhost:27017`, or a different database than `journaldb_local` |
| `server.port` | `SERVER_PORT` | No | Defaults to `8080` |

The app stores its data in the local database **`journaldb_local`** (created automatically). An older local `journaldb` may hold legacy users without an e-mail; those break the app's unique e-mail index, so that database is deliberately not used.

### 3. Start

```bash
./mvnw spring-boot:run
```

(or run `JournalApplication` from IntelliJ). The app listens on **http://localhost:8080**.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Quick check: `curl http://localhost:8080/public/health` → `OK`

---

## 📌 API Endpoints

Send the JWT returned by `/public/login` as `Authorization: Bearer <token>` on every authenticated request.

### 🌐 Public

| Method | Endpoint | Description |
|---|---|---|
| GET | `/public/health` | Health check (`OK`) |
| POST | `/public/signup` | Register — `{"userName", "email", "password"}` |
| POST | `/public/login` | Log in — `{"userName", "password"}` — returns a JWT |
| GET | `/public/test-mail` | Send a test e-mail to the user identified by the token |
| GET | `/oauth2/authorization/google` | Start "Login with Google" |

### 🔐 User (authenticated)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/user` | Greeting with the current weather |
| POST | `/user` | Create a user |
| PUT | `/user` | Update the logged-in user |
| DELETE | `/user` | Delete the logged-in user |

### 📓 Journal entries (authenticated)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/journal` | Get all entries of the logged-in user |
| POST | `/journal` | Create an entry |
| GET | `/journal/id/{id}` | Get one entry |
| PUT | `/journal/id/{id}` | Update an entry |
| DELETE | `/journal/id/{id}` | Delete an entry |
| GET | `/journal/ping` | Ping (`OK`) |

### 🛡 Admin (`ADMIN` role)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/admin/all-users` | List all users |
| POST | `/admin/create-admin-user` | Create an admin user |
| GET | `/admin/clear-app-cache` | Reload the app config cache from MongoDB |

---

## 🌦 Weather Integration

`GET /user` greets the logged-in user with the current "feels like" temperature (city: Mumbai), fetched from [weatherstack](https://weatherstack.com).

The request URL template is read from MongoDB, not from code. Insert this document into the `config_journal_app` collection of the `journaldb_local` database (then call `GET /admin/clear-app-cache` or restart the app):

```json
{ "key": "WEATHER_API", "value": "http://api.weatherstack.com/current?access_key=<apiKey>&query=<city>" }
```

`<apiKey>` is replaced with `weather.api.key` and `<city>` with the city. Without the document, `GET /user` cannot build the weather request.

---

## ⚡ Redis Caching

Redis is used to **cache external API responses** to reduce repeated API calls.

Benefits:

- Faster response time
- Reduced API usage
- Improved performance

Cached data expires automatically using **TTL**. Redis is optional at runtime: if it is down, the app logs the error and simply skips the cache.

---

## 📧 Email Service

The application can send emails using **SMTP configuration**.

Uses:

- JavaMailSender
- Gmail SMTP (needs `MAIL_USERNAME` / `MAIL_PASSWORD` — a Gmail App Password)
- Notification support (a weekly mood e-mail is scheduled for Sundays 09:00)

---

## 🗄 MongoDB transactions

Creating, updating and deleting journal entries is `@Transactional`, and MongoDB only supports transactions on a **replica set**. A plain local `mongod` is a standalone server, so on startup the app detects this, logs a warning, and runs those methods **without** a transaction — everything works, just not atomically.

To get real transactions locally, run `mongod` as a single-node replica set:

1. Add to `/opt/homebrew/etc/mongod.conf`:
   ```yaml
   replication:
     replSetName: rs0
   ```
2. `brew services restart mongodb-community@7.0`
3. Initialise it once with mongosh (`brew install mongosh`):
   `mongosh --eval 'rs.initiate({_id: "rs0", members: [{_id: 0, host: "localhost:27017"}]})'`
4. Start the app with `SPRING_DATA_MONGODB_URI='mongodb://localhost:27017/?replicaSet=rs0'`

The startup log then says `MongoDB transactions: ENABLED`.

---

## 🧪 Testing

```bash
./mvnw test
```

The project includes unit testing using:

- **JUnit**
- **Mockito**
- **Parameterized Tests**

Tested components include:

- UserService
- EmailService
- UserDetailsServiceImpl

The repository test uses the `test` profile (`src/test/resources/application-test.yml`), which reads from the local MongoDB database `testdb`, so MongoDB must be running. The e-mail, Redis and service tests that need external systems are disabled.

---

## 🔒 Security

Security is implemented using **Spring Security**.

Features include:

- JWT bearer-token authentication, plus optional Google OAuth2 login
- Password hashing using BCrypt
- Role-based access control
- Secure endpoints (everything except `/public/**`, `/oauth2/**` and Swagger needs a token)
- No secrets in the repository: `config/` is git-ignored (only `*.example` templates are tracked)

---

## 👤 Author

**Divyansh Kumar**  
Backend Developer | Java & Spring Boot  
GitHub: https://github.com/DIVYANSHGUPTA-5
