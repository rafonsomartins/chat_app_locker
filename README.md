# SecureCard Chat

A secure messaging app for Android that uses NFC smart cards for enhanced encryption and security.

## Features

### User Authentication
- Username/password-based authentication
- Securely stored credentials with password hashing

### NFC Card Integration
- Register whether a user has a secure NFC card
- Decrypt messages using NFC card authentication
- Detect if recipients have NFC cards for higher security

### Message Encryption
- All messages are encrypted on the device (not on the server)
- Standard encryption for all messages
- Additional card-based encryption for users with NFC cards
- Warning when sending to users without secure cards
- Option to silence warnings for specific contacts

### Privacy
- Temporarily decrypted messages disappear after a configurable timeout
- No server-side message storage or processing
- No collection of IP addresses or other tracking information

### User Interface
- Clean, modern material design
- Visual indicators for secure card users
- Contact list with search functionality
- Secure message indicators

## Technical Details

### Encryption
- AES-GCM encryption for standard messages
- NFC card-based encryption for higher security
- Locally generated and stored encryption keys

### NFC Implementation
- Uses IsoDep for NFC card communication
- APDU commands for card communication
- Support for various NFC card technologies

### Local Database
- Room database for local message storage
- Message synchronization while maintaining security

## Development

### Requirements
- Android Studio Arctic Fox or newer
- Android SDK 24+
- A physical device with NFC capabilities for testing card functionality

### Building
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on a device

## Note

This app is a demonstration of secure messaging concepts using NFC cards. For a production environment, additional security measures and thorough security auditing would be required. 