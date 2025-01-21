# BookSmart

## Overview
BookSmart is a Java-based application developed with the Gradle build system. This project is designed to manage and organize books efficiently, offering users an intuitive interface and powerful features to handle their book collections. This repository includes all necessary files for building, running, and contributing to the project.

---

## Features
- **User-Friendly Interface**: Simplified design for easy navigation.
- **Book Management**: Add, update, delete, and view book details.
- **Data Persistence**: Saves data locally for offline access.
- **Modular Design**: Clean and maintainable architecture using modern software development practices.

---

## Project Structure
```
BookSmart OriginalVersion/
├── app/                # Source code and main application logic
├── gradle/             # Gradle wrapper files
├── .gradle/            # Build cache and temporary files (auto-generated)
├── .idea/              # IntelliJ IDEA project settings
├── .vscode/            # VS Code project settings
├── build.gradle        # Gradle build configuration
├── settings.gradle     # Gradle settings
├── gradlew             # Gradle wrapper script (Linux/Mac)
├── gradlew.bat         # Gradle wrapper script (Windows)
├── local.properties    # Local environment configurations
└── README.md           # Project documentation (this file)
```

---

## Prerequisites
To build and run this project, ensure you have the following installed:
- **Java Development Kit (JDK)**: Version 11 or higher.
- **Gradle**: Version 7.0 or higher.
- **Android Studio** (if applicable) or any Java-compatible IDE such as IntelliJ IDEA or Visual Studio Code.

---

## Getting Started
### Clone the Repository
```bash
git clone https://github.com/yourusername/BookSmart.git
cd BookSmart
```

### Build the Project
Run the following command to build the project:
```bash
./gradlew build
```
For Windows:
```cmd
gradlew.bat build
```

### Run the Application
To run the project, use:
```bash
./gradlew run
```

---

## Configuration
### Environment Variables
Edit the `local.properties` file to configure local development settings such as:
- Database paths
- API keys (if any)

### IDE Setup
For IntelliJ IDEA:
1. Open the project folder.
2. Sync the Gradle project when prompted.

For Visual Studio Code:
1. Install the Java Extension Pack.
2. Open the project folder and configure tasks.

---

## Contributing
We welcome contributions! To get started:
1. Fork this repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes with clear messages.
4. Push to your fork and submit a pull request.

---
