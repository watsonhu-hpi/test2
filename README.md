# Chat Application

A full-stack Java chat application built with Spring Boot and React.

## Features

- Real-time messaging using WebSockets
- User authentication and authorization
- Group chats and direct messages
- File uploads and attachments
- Message reactions and typing indicators
- Read receipts
- Contact management
- User presence (online/offline status)
- Message search
- Notifications
- Responsive UI

## Technology Stack

### Backend
- Java 17
- Spring Boot 2.7.0
- Spring Security with JWT authentication
- Spring WebSocket for real-time communication
- Spring Data JPA for database access
- H2 Database (can be replaced with MySQL/PostgreSQL for production)
- Maven for dependency management

### Frontend
- React 18
- Material-UI for UI components
- SockJS and STOMP for WebSocket communication
- React Router for navigation
- Zustand for state management
- Axios for HTTP requests
- Formik and Yup for form validation

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- npm or yarn

### Running the Application

#### Backend
1. Navigate to the project root directory
2. Run `mvn spring-boot:run`
3. The server will start on `http://localhost:8080`

#### Frontend
1. Navigate to the `client` directory
2. Run `npm install` or `yarn install`
3. Run `npm start` or `yarn start`
4. The client will start on `http://localhost:3000`

## API Documentation

The application provides RESTful API endpoints for all functionality:

- `/api/auth/*` - Authentication endpoints
- `/api/users/*` - User management
- `/api/chats/*` - Chat management
- `/api/messages/*` - Message operations
- `/api/notifications/*` - Notification management

WebSocket endpoints:
- `/ws` - WebSocket connection point
- `/app/chat/{chatId}/send` - Send messages
- `/app/chat/{chatId}/typing` - Typing indicator
- `/topic/chat/{chatId}` - Chat message subscription

## Deployment

For production deployment:
1. Configure a production database (MySQL/PostgreSQL)
2. Update security settings in `application.properties`
3. Build the React client: `npm run build`
4. Build the Spring Boot application: `mvn clean package`
5. Deploy the resulting JAR file to your server

## License

[MIT](LICENSE)

## Acknowledgements

- Spring Boot
- React
- Material-UI
- And all other open-source libraries used in this project