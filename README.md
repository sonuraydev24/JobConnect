# JobConnect – Java Full Stack Job Portal

## Stack
Java 21, Spring Boot 3.5.5, Spring Security, Spring Data JPA, Thymeleaf, H2, Maven.

## Features
- Public job listing and job details
- Job Seeker and Recruiter registration/login
- Admin seeded automatically
- Recruiter can post, close and reopen jobs
- Job seeker can apply once per job and track status
- Recruiter can review applicants and update status
- Admin dashboard and enable/disable accounts
- Local persistent H2 database

## Run
Requirements: Java 21 and Maven 3.9+
```powershell
mvn clean install
java -jar target/jobconnect-1.0.0.jar
```
Open http://localhost:8080

## Demo Admin
Email: admin@jobconnect.local
Password: Admin@12345

Change credentials before public deployment with `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables.

This is an educational starter project, not production-ready. Add email verification, password reset, audit logs, secure resume storage/scanning, rate limiting and HTTPS before public use.
