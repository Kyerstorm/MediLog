# Security Features

## Overview
Health Calendar includes comprehensive security features to protect your health data.

## Features

### Passcode Lock
- 4, 6, or 8 digit PIN codes
- Alphanumeric passwords (optional)
- Biometric authentication (fingerprint/face)

### Auto-Lock
- Immediate lock when app backgrounded
- Configurable timeouts: 1, 5, 15, 30 minutes
- Lock on screen off option
- Remember unlock for X minutes

### Failed Attempt Protection
- Configurable max attempts: 3, 5, or 10
- Progressive lockout: 30s → 1min → 5min → 15min → 30min
- Option to require biometric after failures

### Emergency Access
- Security question recovery
- Email verification (future)
- Clear all data option (with warning)

## Usage

### Setting Up Security
1. Go to Settings → Security
2. Enable Passcode
3. Choose passcode length
4. Set up emergency recovery methods

### Recovering Access
If you forget your passcode:
1. Tap "Forgot Passcode?" on lock screen
2. Choose recovery method:
   - Answer security question
   - Request email reset (if configured)
   - Clear all data (last resort)

## Technical Details

### Data Storage
- Passcodes: SHA-256 hashed, stored in DataStore
- Security answers: SHA-256 hashed
- Biometric: Uses Android BiometricPrompt API

### Lockout Algorithm
```
Attempts 1-5: No lockout
Attempt 6: 30 second lockout
Attempt 7: 1 minute lockout
Attempt 8: 5 minute lockout
Attempt 9: 15 minute lockout
Attempt 10+: 30 minute lockout
```
