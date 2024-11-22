# Welcome to Bellevue

Bellevue is a recipe sharing platform inspired by games like Animal Crossing and Neopets but prioritizing the end user's over-all health and happiness by encouraging local friendship and community and offline experiences.

```
       ,
       \`-._           __
        \\  `-..____,.'  `.
         :`.         /    \`.
         :  )       :      : \
          ;'        '   ;  |  :
          )..      .. .:.`.;  :
         /::...  .:::...   ` ;
         ; _ '    __        /:\
         `:o>   /\o_>      ;:. `.
        `-`.__ ;   __..--- /:.   \
        === \_/   ;=====_.':.     ;
         ,/'`--'...`--....        ;
              ;                    ;
            .'                      ;
          .'                        ;
        .'     ..     ,      .       ;
       :       ::..  /      ;::.     |
      /      `.;::.  |       ;:..    ;
     :         |:.   :       ;:.    ;
     :         ::     ;:..   |.    ;
      :       :;      :::....|     |
      /\     ,/ \      ;:::::;     ;
    .:. \:..|    :     ; '.--|     ;
   ::.  :''  `-.,,;     ;'   ;     ;
.-'. _.'\      / `;      \,__:      \
`---'    `----'   ;      /    \,.,,,/
                   `----`
```

## Developer Requirements

To run or contribute to the Bellevue platform, your development environment must include the following dependencies:

- **JDK 23**
- **Docker Compose 2.30**
- **Maven 3.9**
- **VSCode 1.94**
- **Node.js 23.2**
- **NPM 10.9**

Each of these tools is essential for running the Bellevue platform locally, managing dependencies, and building the application. Follow the steps below to get your local machine ready.

---

### 1. JDK 23

The Bellevue platform is built using Java 23, and you must install it to run and build the application. To install JDK 23:

#### On macOS:

1. **Homebrew** is the easiest way to install JDK 23. If you don't have Homebrew, install it by running:
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
2. Install JDK 23 with Homebrew:
   ```bash
   brew install openjdk@23
   ```
3. After installation, you may need to add the JDK to your system's `PATH`:
   ```bash
   export PATH="/opt/homebrew/opt/openjdk@23/bin:$PATH"
   ```

#### On Windows:

1. Download JDK 23 from the [official JDK site](https://jdk.java.net/23/).
2. Run the installer and follow the prompts.
3. Set the `JAVA_HOME` environment variable to the installation directory.

#### On Linux:

1. On Ubuntu, you can install JDK 23 using the following commands:
   ```bash
   sudo apt update
   sudo apt install openjdk-23-jdk
   ```
2. Set the `JAVA_HOME` environment variable:
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-23-openjdk-amd64
   ```

### 2. Docker Compose 2.30

Docker Compose is used to manage multi-container Docker applications. Install Docker Compose to set up and run Docker containers.

#### Installation:

1. **macOS/Windows**: Install Docker Desktop, which includes Docker Compose, from [here](https://www.docker.com/products/docker-desktop).
2. **Linux**: Install Docker Compose manually:
   ```bash
   sudo curl -L "https://github.com/docker/compose/releases/download/2.30.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```

### 3. Maven 3.9

Maven is used to manage project dependencies and build the application.

#### On macOS:

1. Use **Homebrew** to install Maven:
   ```bash
   brew install maven
   ```

#### On Windows:

1. Download the latest Maven version from the [Maven website](https://maven.apache.org/download.cgi).
2. Extract the archive and set the `MAVEN_HOME` environment variable.

#### On Linux:

1. Use **APT** to install Maven:
   ```bash
   sudo apt update
   sudo apt install maven
   ```

### 4. VSCode 1.94

Visual Studio Code (VSCode) is the recommended IDE for working on the Bellevue platform.

#### Installation:

1. Download and install the latest version of VSCode from [here](https://code.visualstudio.com/Download).
2. Install the necessary extensions:
   - **Java Extension Pack**
   - **Docker**
   - **Prettier - Code formatter**
   - **CheckStyle for Java**

### 5. Node.js 23.2

Node.js is essential for managing JavaScript runtime and building the frontend of the Bellevue platform.

#### Installation:

1. Use **NVM** (Node Version Manager) for easy installation and switching between Node versions:

   - First, install NVM by running:
     ```bash
     curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash
     ```
     or for Windows, follow the [install guide for NVM on Windows](https://github.com/coreybutler/nvm-windows).
   - Install Node.js 23.2 with NVM:
     ```bash
     nvm install 23.2
     nvm use 23.2
     ```

### 6. NPM 10.9

NPM is the package manager for Node.js, used to install frontend dependencies.

#### Installation:

1. After installing Node.js, NPM will be installed automatically. You can check the version with:
   ```bash
   npm -v
   ```

---

## Running the Development Environment from the Terminal

Once the dependencies are installed, follow the steps below to set up and run the Bellevue platform locally.

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/your-repo/bellevue.git
   cd bellevue
   ```

2. **Start Docker**
   The MYSQL database and Redis caching layer for the development environment are served using Docker. Start the images described in the docker-compose.yml file using Docker Compose.

   ```bash
   docker-compose up -d
   ```

3. **Build the Frontend**:
   The frontend is built with React. Navigate to the `src/main/react/bellevue` directory and install the necessary dependencies:

   ```bash
   cd src/main/react/bellevue
   npm install
   npm run build
   ```

   This will output the production build of the React frontend into the `src/main/resources/target/` directory, where the Spring application will serve it at index.html.

4. **Build and Run Webapp**:
   The backend is a Spring Boot application, and you can run it from the base directory using Maven:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the Application**:
   Open your browser and navigate to [http://localhost:8080](http://localhost:8080) to see the Bellevue platform running locally.

---

# Happy Coding!
