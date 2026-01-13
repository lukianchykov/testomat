# Testomat.io JUnit 5 Extension

A powerful JUnit 5 extension that automatically reports test execution results to [Testomat.io](https://testomat.io), providing real-time visibility into your test runs with detailed reporting and analytics.

## 🚀 Features

* **Automatic Test Reporting** - Seamlessly reports all test results to Testomat.io
* **Test Run Management** - Creates, tracks, and finalizes test runs automatically
* **Detailed Status Tracking** - Captures passed, failed, and skipped test statuses
* **Rich Error Information** - Reports error messages and stack traces for failed tests
* **Custom Annotations** - Use `@TestId` and `@Title` to enhance your test metadata
* **Zero Configuration** - Simple setup with just an API key

## 📋 Prerequisites

* Java 8 or higher
* JUnit 5 (Jupiter)
* Maven 3.6+
* A [Testomat.io](https://app.testomat.io) account (free to sign up)

## 📦 Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.testomat</groupId>
    <artifactId>java-reporter-junit</artifactId>
    <version>0.8.14</version>
    <scope>compile</scope>
</dependency>
```

## 🔧 Configuration

### 1. Get Your API Key

1. Sign up for a free account at [app.testomat.io](https://app.testomat.io)
2. Create a new project or select an existing one
3. Navigate to **Settings** → **Project Reporting API key**
4. Copy your API key (starts with `tstmt_`)

### 2. Set Environment Variable

Set the `TESTOMATIO` environment variable with your API key:

**Windows (Command Prompt):**

```cmd
set TESTOMATIO=tstmt_your_api_key_here
```

**Windows (PowerShell):**

```powershell
$env:TESTOMATIO="tstmt_your_api_key_here"
```

**IDE Configuration (IntelliJ IDEA):**

1. Go to **Run** → **Edit Configurations**
2. Select your test configuration
3. Add environment variable: `TESTOMATIO=tstmt_your_api_key_here`

## ▶️ Running Tests

You can run your tests (and automatically report results to Testomat.io) using Maven:

```bash
mvn clean install
```

This command will:

1. Clean previous build artifacts
2. Compile the project
3. Run all JUnit 5 tests
4. Send test execution results to Testomat.io automatically


## 🔍 Viewing Results

After running your tests:

1. Go to [app.testomat.io](https://app.testomat.io)
2. Navigate to your project
3. Click on **Test Runs** in the sidebar
4. View your latest test run with detailed results
5. Analyze trends, failures, and performance metrics

## 🛠️ How It Works

The extension implements JUnit 5's extension points:

1. **BeforeAllCallback** - Creates a test run in Testomat.io before tests start
2. **TestWatcher** - Monitors each test execution and reports results
3. **AfterAllCallback** - Finalizes the test run with duration and status
4. **TestExecutionExceptionHandler** - Captures and reports test failures

### API Integration

The extension makes three key API calls:

#### 1. Create Test Run

```http
POST https://app.testomat.io/api/reporter?api_key=YOUR_KEY
Content-Type: application/json

{
  "title": "My Awesome Junit 5 Test Run"
}
```

#### 2. Report Test Result

```http
POST https://app.testomat.io/api/reporter/{run_uid}/testrun?api_key=YOUR_KEY
Content-Type: application/json

{
  "title": "Verify basic addition operation",
  "test_id": "T12345",
  "suite_title": "TestomatExampleTest",
  "file": "io/testomat/junit5/TestomatExampleTest.java",
  "status": "passed"
}
```

#### 3. Finish Test Run

```http
PUT https://app.testomat.io/api/reporter/{run_uid}?api_key=YOUR_KEY
Content-Type: application/json

{
  "status_event": "finish",
  "duration": 25.5
}
```
