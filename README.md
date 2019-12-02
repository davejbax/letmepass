# LetMePass

Android password manager with a modern UI and cloud storage (via Google Drive) support

<a href="/davejbax/letmepass/blob/master/screenshots/intro-screen.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/intro-screen.png" alt="Intro screen" /></a> <a href="/davejbax/letmepass/blob/master/screenshots/creation-step-1.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/creation-step-1.png" alt="Creation screen" /></a> <a href="/davejbax/letmepass/blob/master/screenshots/main-list.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/main-list.png" alt="Passwords screen" /></a>

### Why?
This project was initially developed as part of coursework for an A-level in Computer Science. The aim of the project was to create a modern, free, open source password manager that resolved some of the gripes that I had with existing solutions, such as: dated and difficult-to-use UI; weak or unexplicated encryption routines; no/limited mobile support; and no free cloud storage support.

LetMePass links in with Google Drive to allow for automatic backup and persistence of password databases, with no central service or premium options or subscriptions. It is an Android application (sorry iOS users!) and therefore is as portable as your device itself; it is further designed to be compatible with almost any Android device.

### Features
- Password, folder, and data storage in the password database
- Unlimited password databases
- Password security checking, with optional [breach checking](https://haveibeenpwned.com/Passwords)
- Local database file support
- Cloud database file support (with Google Drive)
- Favourite passwords for organization
- Search and sort passwords

### Technical Details
- AES-256 with GCM encryption of the password database (with hybrid random/sequential IV generation)
- Custom file format with header integrity (SHA256 hash prepended to database payload and then encrypted)
- Argon2 (winner of the Password Hashing Competition) used to derive keys for encryption from passwords

### Screenshots
<a href="/davejbax/letmepass/blob/master/screenshots/intro-screen.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/intro-screen.png" alt="Intro screen" /></a>
<a href="/davejbax/letmepass/blob/master/screenshots/creation-step-1.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/creation-step-1.png" alt="Creation screen step 1" /></a>
<a href="/davejbax/letmepass/blob/master/screenshots/creation-step-2.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/creation-step-2.png" alt="Creation screen step 2" /></a>
<a href="/davejbax/letmepass/blob/master/screenshots/main-list.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/main-list.png" alt="Passwords screen" /></a>
<a href="/davejbax/letmepass/blob/master/screenshots/main-password.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/main-password.png" alt="Password viewing screen" /></a>
<a href="/davejbax/letmepass/blob/master/screenshots/main-search.png"><img width="250" src="/davejbax/letmepass/raw/master/screenshots/main-search.png" alt="Search functionality" /></a>

### Development Progress
The app is not fully complete (as evidenced by the 'TODOs' in the codebase). Most features are implemented, but currently several features that are somewhat integral to everyday use are lacking, including settings (i.e. changing app and database settings), auto-loading databases on app launch, and database locking on app minimize.

### Libraries Used
- Cryptography
  * Argon2, via [argon2-jvm](https://github.com/phxql/argon2-jvm) binding (+ [JNA](https://github.com/java-native-access/jna))
  * [Bouncy Castle](https://www.bouncycastle.org/)
- UI
  * [FloatingActionButton](https://github.com/futuresimple/android-floating-action-button)
  * [Android Material Stepper](https://github.com/stepstone-tech/android-material-stepper)
  * Android support libraries
- Storage
  * Google Drive API for Android
  * [Gson](https://github.com/google/gson)
  * [SQLDroid](https://github.com/google/gson)
- Other
  * [Apache Commons Text](https://commons.apache.org/proper/commons-text/)
  * [OkHTTP](https://square.github.io/okhttp/)
