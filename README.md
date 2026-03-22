# JournalApp

A secure Spring Boot REST API for managing journal entries with authentication, weather integration, caching, and email support.

---

## 🚀 Features

- User authentication (Spring Security)
- Journal CRUD operations
- MongoDB integration
- Redis caching for faster responses
- Weather API integration
- Email notification support
- Role-based access control
- Secure password encryption (BCrypt)
- Sentiment analysis support for journal entries
- Scheduled tasks using Spring Scheduler
- Environment variable-based configuration (no secrets in repo)
- Unit testing with JUnit and Mockito

---

## 🛠 Tech Stack

- Java 21
- Spring Boot
- Spring Security
- MongoDB Atlas
- Redis
- Maven
- REST APIs
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

## 📌 API Endpoints

### 🔐 User

| Method | Endpoint | Description |

POST | `/journal/user` | Register new user |
PUT | `/journal/user` | Update logged-in user |
DELETE | `/journal/user` | Delete logged-in user |

---

### 📓 Journal Entries

| Method | Endpoint | Description |

POST | `/journal/journal` | Create journal entry |
GET | `/journal/journal` | Get all user journal entries |
PUT | `/journal/journal/id/{id}` | Update journal entry |
DELETE | `/journal/journal/id/{id}` | Delete journal entry |

---

## 🌦 Weather Integration

The application integrates with an external **Weather API**.

When a user accesses the greeting endpoint:

---

## ⚡ Redis Caching

Redis is used to **cache external API responses** to reduce repeated API calls.

Benefits:

- Faster response time
- Reduced API usage
- Improved performance

Cached data expires automatically using **TTL**.

---

## 📧 Email Service

The application can send emails using **SMTP configuration**.

Uses:

- JavaMailSender
- Gmail SMTP
- Notification support

---

## 🧪 Testing

The project includes unit testing using:

- **JUnit**
- **Mockito**
- **Parameterized Tests**

Tested components include:

- UserService
- EmailService
- UserDetailsServiceImpl

---

## 🔒 Security

Security is implemented using **Spring Security**.

Features include:

- HTTP Basic authentication
- Password hashing using BCrypt
- Role-based access control
- Secure endpoints

---

## ▶ Running the Project

1. Clone the repository
---

## 👤 Author

**Divyansh Kumar**  
Backend Developer | Java & Spring Boot  
GitHub: https://github.com/DIVYANSHGUPTA-5

