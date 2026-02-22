# Environment Setup

This application uses environment variables to secure sensitive configuration.

## Local Development Setup

1. Copy the template file:
   ```bash
   cp src/main/resources/application.properties.template src/main/resources/application.properties
   ```

2. Create a `.env` file with your secrets:
   ```bash
   # Database Credentials
   DB_USERNAME=root
   DB_PASSWORD=your_mysql_password

   # RabbitMQ Credentials
   RABBITMQ_USERNAME=guest
   RABBITMQ_PASSWORD=guest

   # SendGrid Configuration
   SENDGRID_API_KEY=your_sendgrid_api_key
   SENDGRID_FROM_EMAIL=your_email@gmail.com
   ```

3. Load environment variables before running:
   ```bash
   export $(cat .env | xargs)
   mvn spring-boot:run
   ```

## Production Deployment

Set these environment variables in your deployment environment:
- `DB_USERNAME`
- `DB_PASSWORD`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
- `SENDGRID_API_KEY`
- `SENDGRID_FROM_EMAIL`

## Security Notes

- Never commit `.env` or `application.properties` with real secrets
- Use different credentials for production
- Rotate API keys regularly
- Use vault services for production secrets management
