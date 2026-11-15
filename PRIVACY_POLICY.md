# Privacy Policy for IrisFit

**Effective Date:** November 8, 2026  
**App Name:** IrisFit  
**Package Name:** com.eyeoverlay.app  

IrisFit ("we", "our", or "us") values your privacy. This Privacy Policy explains how IrisFit collects, uses, and protects your information when you use our mobile application.

---

## 1. Information We Collect
IrisFit stores guided-session completion records locally. Some operating-system APIs and bundled libraries may have separate processing behavior; this policy does not treat the app's local record file as proof that every SDK or system service is offline.
- **Screen Time Data (`UsageStatsManager`):** If you grant Usage Access, IrisFit queries Android's app-usage statistics for the current local day to calculate an optional screen-time wellness indicator. Raw per-app usage history is not stored in the guided-session completion file. This statement does not establish the network behavior of unrelated SDKs or Android system services.
- **Sensor Data (Accelerometer, Gyroscope, Proximity, Light):** Sensor values are processed in memory to estimate device posture. Posture does not confirm that your eyes are resting. Raw sensor values are not stored in the guided-session completion file.
- **Community & Relay Data (Optional):** The current app build should be treated as local-only unless a separately documented network feature is enabled. Do not assume relay or challenge information is synchronized to a server; verify the actual build and service configuration before publishing this statement.

## 2. How We Use Your Information
We use the collected information solely for the following purposes:
- To calculate an in-app wellness habit indicator from permitted screen-usage statistics and locally stored completion records.
- To display daily and weekly app-recorded habit trends.
- To operate locally available reminders and milestone features.

## 3. Data Storage and Security
Guided-session completion records are stored in an app-private local file using Android AtomicFile. This file is not encrypted by the application. Screen-time aggregates are queried from Android UsageStatsManager when permission is granted; the app does not persist a raw per-app usage history in its own completion ledger. We do not operate external central databases that store your personal screen time or sensor telemetry.

## 4. Third-Party Services
The app build includes Google's ML Kit GenAI Prompt SDK and AICore integration code. The AI Coach entry point is disabled while data handling is reviewed. The app has not independently verified all SDK or system-service network communication, retention, or model-training behavior; no claim that all app or SDK data stays on-device should be inferred. We will update this section before enabling the AI Coach.

The reviewed production dependencies do not declare Firebase Analytics or Crashlytics runtime dependencies. This does not by itself establish the data behavior of other SDKs or Android system services.

## 5. Children's Privacy
IrisFit is suitable for all ages and does not knowingly collect personal information from children under 13 without parental consent.

## 6. Changes to This Privacy Policy
We may update our Privacy Policy from time to time. Any changes will be posted within this document with an updated effective date.

## 7. Contact Us
If you have any questions or concerns about this Privacy Policy, please contact us at:
- **Email:** support@irisfit.app
