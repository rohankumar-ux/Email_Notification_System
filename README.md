# Email Notification System

A modern full-stack application for managing and sending email notifications with a responsive web interface.

## Overview

The Email Notification System is a comprehensive solution that provides:
- Email management and scheduling capabilities
- Real-time notification tracking
- User-friendly web interface for managing emails
- Backend API with robust email processing

## Architecture

This project follows a modern microservices architecture with:

### Backend (Spring Boot)
- RESTful API built with Spring Boot 4.0.2
- Java 21 with Maven build system
- MySQL database with JPA/Hibernate
- SendGrid integration for email delivery
- RabbitMQ for message queuing
- Comprehensive testing with JUnit 5 and Mockito
- Code coverage with JaCoCo

### Frontend (React)
- Modern React application with Vite
- Responsive UI with React Router
- Data visualization with Recharts
- HTTP client with Axios
- Toast notifications with react-hot-toast
- ESLint for code quality

## Key Features

- **Email Management**: Create, schedule, and track email notifications
- **Real-time Updates**: Live status updates for email delivery
- **Analytics**: Visual dashboards for email performance metrics
- **Queue Processing**: Asynchronous email processing with RabbitMQ
- **Validation**: Input validation and error handling
- **Testing**: Comprehensive unit and integration tests

## Technology Stack

### Backend
- Spring Boot 4.0.2
- Java 21
- MySQL Database
- SendGrid API
- RabbitMQ
- Maven
- JUnit 5
- Mockito

### Frontend
- React 19
- Vite
- React Router
- Axios
- Recharts
- ESLint

## Getting Started

### Prerequisites
- Java 21
- Node.js 18+
- MySQL
- RabbitMQ
- SendGrid API key

### Installation

1. Clone the repository
2. Set up the backend database and configure properties
3. Run the Spring Boot application
4. Set up the frontend dependencies
5. Start the React development server

## Project Structure

```
EmailNotificationSystem/
├── backend/          # Spring Boot API
├── frontend/         # React application
├── .github/          # CI/CD workflows
└── README.md         # This file
```

## Contributing

This project uses industry-standard practices including:
- Git version control
- CI/CD pipelines
- Code quality tools
- Comprehensive testing
- Documentation



## License

This is a demo project for educational and development purposes.
