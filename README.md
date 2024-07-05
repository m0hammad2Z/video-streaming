# Video Streaming System

Containerized video streaming system, leveraging microservices and Docker for modularity, scalability, and efficient development.
## Features

* User authentication for secure video upload and viewing
* Video upload with format validation (MP4)
* Video streaming for authenticated users
* Database storage of video information

## Architecture

The system is comprised of five microservices:

* **Upload Video (Web App):** Handles user authentication, video upload validation, and stores video information in a MySQL database.
* **Video Streaming (Web App):** Enables authenticated users to view videos by retrieving video details from the database and streaming content from the File System service.
* **Authentication Service:** Validates user credentials for secure access.
* **File System Service:** Provides functionality to write videos to persistent storage.
* **MySQL DB Service:** Stores video information for efficient retrieval and management.

## Technologies

* **Backend:**
    * Upload Video & File System Services: Spring Boot (Java)
    * Video Streaming Service: Node.js
    * Authentication Service: Spring Boot (Java)
* **Database:** MySQL
* **Containerization:** Docker
* **Orchestration:** Docker Compose
* **CI/CD:** GitHub Actions

## Getting Started

**Prerequisites:**

* Docker installed (https://www.docker.com/)
* Docker Hub account (optional, for deploying images)

1. Clone the repository:

```bash
git clone [https://github.com/m0hammad2Z/video-streaming.git](https://github.com/m0hammad2Z/video-streaming.git)
```
2. Navigate to the project directory:
```bash
cd video-streaming
```

3. Build and start the application using Docker Compose:
```bash
docker-compose up -d
```
This will build Docker images for each service, start the containers, and run the application in the background.

4. Access the application (assuming ports are not modified in docker-compose.yml):
* Upload Video: http://localhost:8080
* Video Streaming: http://localhost:3000


## License
MIT License [Link to MIT License](https://opensource.org/licenses/MIT)
