
```kotlin
RL.init(
    application = this,
    host = "http://0.0.0.0:8080",
    appKey = "APP_KEY",
    clientId = deviceId() // Custom unique device identifier (Default random)
)
```

```kotlin
/**
 Optional specifications
 */
// Firebase token
RL.firebaseToken = "USER_APP_FIREBASE_TOKEN"
// Custom unique user identifier
RL.identification = "user0"
// Extra user info
RL.extraInfo = "09101234567"
```
