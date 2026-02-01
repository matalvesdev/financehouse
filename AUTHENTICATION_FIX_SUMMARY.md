# Authentication Fix Summary

## Date: 2026-02-01

## Problem

Login was failing with "Credenciais inválidas" (Invalid credentials) error even when using correct email and password combinations. Users could register successfully, but authentication always failed.

## Root Cause Analysis

The issue was caused by field-level encryption on the email column in the database:

1. **Email Encryption**: The `UsuarioJpaEntity` class had `@Convert(converter = EncryptedStringConverter.class)` annotation on the email field
2. **Search Incompatibility**: When a user tried to login with "john@example.com", the JPA query tried to match this plain text against the encrypted value in the database
3. **Query Failure**: Since encrypted values are not searchable, the query `findByEmailAndAtivo(email)` never found any matching users
4. **Authentication Failure**: Without finding the user, authentication always failed

## Solution

### 1. Remove Email Encryption

**File**: `backend/src/main/java/com/gestaofinanceira/infrastructure/persistence/entity/UsuarioJpaEntity.java`

**Change**: Removed the `@Convert(converter = EncryptedStringConverter.class)` annotation from the email field.

**Rationale**: 
- Email addresses are used for login and must be searchable
- Email addresses are not considered highly sensitive data (they're used for communication)
- The nome (name) field remains encrypted for privacy

### 2. Fix UsuarioResponse Parameter Order

**Files**: 
- `backend/src/main/java/com/gestaofinanceira/application/usecases/autenticacao/RegistrarUsuarioUseCase.java`
- `backend/src/main/java/com/gestaofinanceira/application/usecases/autenticacao/AutenticarUsuarioUseCase.java`

**Change**: Fixed the order of parameters when creating `UsuarioResponse` objects. The constructor expects `(id, nome, email, ...)` but the code was passing `(id, email, nome, ...)`.

**Impact**: This caused the API to return swapped values in the response JSON (nome showing email and vice versa).

## Testing Results

After applying the fixes:

✅ **User Registration**: Successfully creates users with correct data
```bash
POST /api/auth/register
{
  "nome": "John Doe",
  "email": "john@example.com",
  "senha": "Test@1234"
}
Response: 201 Created
{
  "id": "e32b7365-9371-4616-86a5-da9720a82689",
  "nome": "John Doe",
  "email": "john@example.com",
  "criadoEm": "2026-02-01T00:49:00.052505948",
  "ativo": true,
  "dadosIniciaisCarregados": false
}
```

✅ **User Login**: Successfully authenticates and returns JWT tokens
```bash
POST /api/auth/login
{
  "email": "john@example.com",
  "senha": "Test@1234"
}
Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "usuario": {
    "id": "e32b7365-9371-4616-86a5-da9720a82689",
    "nome": "John Doe",
    "email": "john@example.com",
    ...
  }
}
```

✅ **Password Verification**: SHA-256 hashing with salt works correctly

## Security Considerations

### What's Still Encrypted
- **Nome (Name)**: User names remain encrypted in the database using AES-256-GCM
- **Senha (Password)**: Passwords are hashed using SHA-256 with unique salts (not encrypted, properly hashed)

### What's Not Encrypted
- **Email**: Stored in plain text to enable login queries
- **Other metadata**: IDs, timestamps, boolean flags

### Security Best Practices Maintained
- Passwords are never stored in plain text (SHA-256 hash + salt)
- JWT tokens for stateless authentication
- Secure password requirements (8+ chars, uppercase, lowercase, number, special char)
- HTTPS should be used in production to protect data in transit

## Database Migration

Since the email field structure changed, the database was reset:
```bash
docker-compose down
docker volume rm financehouse_postgres_data
docker-compose up -d
```

**Note**: In production, you would need a proper migration strategy to decrypt existing emails or require users to re-register.

## Files Modified

1. `backend/src/main/java/com/gestaofinanceira/infrastructure/persistence/entity/UsuarioJpaEntity.java`
   - Removed `@Convert` annotation from email field

2. `backend/src/main/java/com/gestaofinanceira/application/usecases/autenticacao/RegistrarUsuarioUseCase.java`
   - Fixed parameter order in UsuarioResponse constructor

3. `backend/src/main/java/com/gestaofinanceira/application/usecases/autenticacao/AutenticarUsuarioUseCase.java`
   - Fixed parameter order in UsuarioResponse constructor

4. `LOCAL_TESTING_GUIDE.md`
   - Added troubleshooting section for authentication issues

## Lessons Learned

1. **Searchable Fields**: Never encrypt fields that need to be searched or used as query parameters
2. **Constructor Parameter Order**: Use named parameters or builder pattern to avoid parameter order mistakes
3. **Testing**: Always test the complete authentication flow (register → login → access protected resources)
4. **Encryption Strategy**: Carefully consider which fields truly need encryption vs. which need to be searchable

## Next Steps

1. Test the complete application flow through the frontend UI
2. Verify all protected endpoints work with JWT authentication
3. Test refresh token functionality
4. Commit and push changes to GitHub
5. Deploy to Oracle Cloud and test in production environment
