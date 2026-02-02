# 🏥 Mental Health Support System (MHSA) - Backend

This is the Backend API for the **Mental Health Support Application** (Thesis Project).
It is built using **Java Spring Boot 3** following a Modular Monolith architecture, powered by **PostgreSQL** and **Redis**.

---

## 🛠 Prerequisites

Before starting, ensure your machine has the following installed:

1.  **Java JDK 17**: [Download Here](https://www.oracle.com/java/technologies/downloads/#java17)
2.  **Docker Desktop**: [Download Here](https://www.docker.com/products/docker-desktop/) (Required for Database & Cache)
3.  **Git**: For version control.
4.  **Terminal**: PowerShell (Windows) or Bash (Mac/Linux).

---

## 🚀 Setup & Run Guide

### 1. Infrastructure Setup (Docker)

We use Docker to run PostgreSQL and Redis. You do not need to install these databases manually on your host machine.

1.  Open **Docker Desktop** and ensure the engine is running.
2.  Open your terminal in the project root directory (`thesis-backend`).
3.  Run the following command to start the services:

    ```bash
    docker-compose up -d
    ```

4.  **Verify:** Check Docker Dashboard. You should see 3 containers running:
    - `mhsa_postgres` (Port 5433)
    - `mhsa_redis` (Port 6379)
    - `mhsa_pgadmin` (Port 5050)

> **Note:** The Database port is set to **5433** to avoid conflicts with any local PostgreSQL installations.

---

### 2. Running the Application (Command Line)

You can run the application directly from the terminal without opening an IDE.

**For Windows (PowerShell/CMD):**

```powershell
.\mvnw spring-boot:run

```

**For Mac / Linux:**

```bash
./mvnw spring-boot:run

```

_Note: The first run may take a few minutes to download Maven dependencies._

When you see the following log, the server is ready:

> `Tomcat started on port 8080 (http)`
> `Started BackendServerApplication in ... seconds`

---

### 3. Accessing the System

Once the server is running, you can access the following tools:

#### 📄 API Documentation (Swagger UI)

Test APIs directly in the browser.

- **URL:** [http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)

#### 🗄️ Database Management (pgAdmin)

A web interface to view and manage database tables.

- **URL:** [http://localhost:5050](https://www.google.com/search?q=http://localhost:5050)
- **Email:** `admin@mhsa.com`
- **Password:** `admin`

**To connect to the DB inside pgAdmin:**

1. Right-click **Servers** -> **Register** -> **Server**.
2. **General Tab:** Name = `Local DB`.
3. **Connection Tab:**

- Host name: `host.docker.internal` (or `postgres`)
- Port: `5433`
- Username: `postgres`
- Password: `123456`

---

## 🛑 Useful Commands

| Action              | Command                                        |
| ------------------- | ---------------------------------------------- |
| **Stop Server**     | Press `Ctrl + C` in the terminal               |
| **Clean & Rebuild** | `.\mvnw clean install`                         |
| **Stop Docker**     | `docker-compose down`                          |
| **Reset Database**  | `docker-compose down -v` (⚠️ Deletes all data) |

---

## ⚠️ Troubleshooting

### 1. Error: "Password authentication failed for user postgres"

**Cause:** Docker is retaining an old password configuration in its volume.
**Fix:** You must delete the old volume and restart.

```bash
docker-compose down -v
docker-compose up -d

```

### 2. Error: "Port 5433 / 6379 is already in use"

**Cause:** Another instance of Docker or a local service is using these ports.
**Fix:** Stop the conflicting service or restart Docker.

### 3. Error: "mvnw: The term is not recognized..."

**Cause:** PowerShell execution policy or path issue.
**Fix:** Try running `.\mvnw.cmd spring-boot:run` instead.

### 4. Connection Refused (Spring Boot startup)

**Check:**

- Is Docker Desktop running?
- Is `application.properties` pointing to port `5433`?

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/mhsa_db

```
