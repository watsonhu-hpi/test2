# Login Credentials Collection - Security Implementation

This document outlines the security measures implemented for the credential collection feature to address potential risks.

## Security Measures Implemented

### 1. Encryption of Sensitive Data

- **Password Encryption**: All passwords are encrypted before being stored using Spring Security's `TextEncryptor`.
- **Email Encryption**: User email addresses are also encrypted to protect PII (Personally Identifiable Information).
- **Secure Key Management**: The encryption key is derived from the application's JWT secret but with a different salt.

```java
@PostConstruct
public void init() {
    String salt = Base64.getEncoder().encodeToString("credentialSalt".getBytes()).substring(0, 8);
    encryptor = Encryptors.text(encryptionKey, salt);
}
```

### 2. Enhanced Access Control

- **Role-Based Access Control**: Only users with ADMIN role can access the credential endpoints.
- **Permission-Based Access Control**: Additional fine-grained permissions implemented through `SecurityService`:
  - `VIEW_CREDENTIALS`: Basic permission to view credential records (encrypted)
  - `DECRYPT_CREDENTIALS`: Higher-level permission required to decrypt sensitive data
  - `MANAGE_CREDENTIALS`: Permission to manage credential records (deletion, etc.)

```java
@PreAuthorize("hasRole('ADMIN') and @securityService.hasPermission('VIEW_CREDENTIALS')")
```

### 3. Data Retention Policy

- **Automatic Data Purging**: A scheduled task runs daily to remove credentials older than 30 days.
- **Manual Purging**: Admins can manually purge old credentials through the API.

```java
@Scheduled(cron = "0 0 0 * * ?")
public void purgeOldCredentials() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
    List<LoginAttempt> oldRecords = loginAttemptRepository.findByAttemptTimeBefore(cutoffDate);
    loginAttemptRepository.deleteAll(oldRecords);
}
```

### 4. Comprehensive Audit Logging

- **Access Logging**: All access to credential data is logged with details about:
  - Admin username who accessed the data
  - IP address from which access was made
  - Timestamp of access
  - Type of action performed

- **Enhanced Logging for Sensitive Operations**: Decryption operations have enhanced logging:

```java
logger.warn("SECURITY ALERT: Admin {} decrypted credential ID {} from IP {}", 
         auth.getName(), id, request.getRemoteAddr());
```

## Security Recommendations

For production deployment, consider these additional security measures:

1. **Encryption Key Management**:
   - Store encryption keys in a secure key vault
   - Implement key rotation procedures
   - Consider using a Hardware Security Module (HSM) for key operations

2. **Additional Access Controls**:
   - Implement multi-factor authentication for accessing credential data
   - Add IP restriction for admin access
   - Implement time-based access windows

3. **Data Protection**:
   - Consider implementing field-level encryption at the database level
   - Add database audit trails
   - Implement database encryption at rest

4. **Compliance Considerations**:
   - Ensure compliance with GDPR, CCPA, and other relevant regulations
   - Implement data subject access request handling
   - Document legitimate purposes for storing credentials

## Implementation Classes

- `LoginAttempt.java`: Entity to store login attempts
- `LoginAttemptRepository.java`: Data access for login attempts
- `LoginAttemptService.java`: Service with encryption/decryption logic
- `AdminController.java`: API endpoints with access controls
- `SecurityService.java`: Permission management service

Remember that this implementation is intended for specific security testing or monitoring purposes only, and appropriate safeguards have been put in place to minimize the risks associated with storing credentials.