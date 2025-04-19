# DecisionHelperApp

DecisionHelperApp is an Android application designed to help users make informed decisions through customizable questionnaires. Users can answer questions, rate their importance, and receive tailored recommendations based on their input. Built with a modern MVVM architecture and Firebase integration, the app provides a robust platform for creating, sharing, and analyzing decision-making tools.

---

## **Features**
- Create and answer quizzes to evaluate decisions
- Multiple question types (multiple choice, yes/no) with customizable options
- Upload images to enhance quiz questions
- User authentication with email/password or Google Sign-In
- Save quiz results and track progress over time
- Public and private quiz sharing options
- View and compare scores across different quizzes
- Responsive UI with intuitive navigation

---

## **Technologies Used**

1. **Android (Java)**:  
   The entire application is built in Java within Android Studio, utilizing Android's lifecycle components and UI framework.

2. **XML**:  
   Used for defining the layout and design of the app's UI.

3. **Firebase Authentication**:  
   Handles user authentication, supporting both email/password and Google Sign-In methods.

4. **Cloud Firestore**:  
   A NoSQL cloud database to store and sync app data at scale, including users, quizzes, questions, and scores.

5. **Firebase Storage**:  
   Cloud storage solution for user-generated content such as question images.

6. **SQLite (Local Cache)**:  
   A lightweight local database used for offline access and caching.

7. **Android Architecture Components**:  
   - LiveData: For observable data holder classes
   - ViewModel: For UI-related data handling that survives configuration changes

---

## **Architecture: MVVM (Model-View-ViewModel)**

DecisionHelperApp follows the **MVVM architecture** to ensure a clear separation of concerns, modularity, and ease of maintenance.

### **Model**  
The **Model** layer represents the data structures and business logic of the application.

Files in this layer:
- **Data Models**:
  - `Quiz.java`: Represents a quiz with metadata such as title, description, category, and visibility settings.
  - `Question.java`: Represents quiz questions, supporting multiple choice and yes/no types with weighted answers.
  - `User.java`: Represents a user with authentication data, profile information, and preferences.
  - `QuizQuestions.java`: Manages relationships between quizzes and their questions including ordering.
  - `QuizUser.java`: Tracks user interactions with quizzes including completion status, responses, and results.
  - `Scores.java`: Stores score ranges, interpretations, and feedback for results analysis.
  - `Answer.java`: Represents answer options with associated percentages and position-based weighting.

- **Data Access**:
  - `DatabaseHelper.java`: Manages SQLite database operations for offline functionality with predefined tables for all entities.
  - Various DAO classes: Implement Firebase Firestore operations with callback interfaces for asynchronous data handling:
    - `QuestionDAO.java`: Manages CRUD operations for questions with batch updating capabilities
    - `QuizDAO.java`: Handles quiz creation, retrieval, and management
    - `UserDAO.java`: Manages user profiles and authentication state
    - `ScoresDAO.java`: Processes and stores quiz result data
    - `QuizUserDAO.java`: Tracks relationships between users and quizzes
    - `QuizQuestionsDAO.java`: Manages question collections within quizzes

- **Repository**:
  - `DecisionRepository.java`: Centralizes data operations and abstracts data sources from the rest of the app:
    - Initializes and orchestrates all DAO instances
    - Provides a unified API for ViewModels to access data
    - Implements callback handling for asynchronous Firebase operations
    - Manages data consistency across different collections

### **View**  
The **View** layer handles UI rendering and user interaction.

Components in this layer:
- **Activities**:
  - `BaseActivity.java`: Parent class with shared functionality for all activities.
  - `MainActivity.java`: Primary navigation hub for the application.
  - `LoginActivity.java`: Handles user authentication flow.
  - `CreateQuizActivity.java`: Interface for creating and editing quizzes.
  - `QuizActivity.java`: Displays and processes quiz questions.
  - `ScoresActivity.java`: Shows user performance metrics.

- **Adapters**:
  - `QuizAdapter.java`: Binds quiz data to RecyclerView items.
  - `QuestionAdapter.java`: Manages display of questions in lists.
  - `ScoresAdapter.java`: Handles score data visualization.

- **Layouts**:
  - Various XML layouts defining the UI structure of each screen.

### **ViewModel**  
The **ViewModel** layer processes data for the UI and manages UI-related data in a lifecycle-conscious way using LiveData objects.

Key ViewModels:
- `MainViewModel.java`: Manages main screen state, user data, and profile operations with error handling.
- `AuthUseCase.java`: Implements authentication logic including email/password and Google Sign-In integration.
- `CreateQuizViewModel.java`: Manages quiz creation workflow, question management, image uploads, and Firebase storage operations.
- `QuizViewModel.java`: Controls quiz interaction flow, question navigation, status tracking, and result calculation.
- `ScoresViewModel.java`: Processes and formats score data for display with filtering and sorting capabilities.

---

## **Key Functionality**

### **User Authentication**
- Email/password registration and login with validation
- Google authentication integration using Firebase Auth
- User profile management with real-time updates
- Session persistence across app restarts
- Secure password handling with cryptographic hashing
- Authentication state monitoring across the application

### **Quiz Creation and Management**
- Multi-step quiz creation process with progress tracking
- Support for multiple choice and yes/no question types with customizable weightings
- Drag-and-drop reordering of answer options with automatic percentage adjustment
- Image upload capability for questions with Firebase Storage integration
- Comprehensive input validation for quiz content
- Public/private visibility options for sharing control
- Real-time preview of quiz appearance during creation

### **Quiz Taking**
- Sequential or random question presentation with smooth navigation
- Interactive progress tracking during quiz sessions
- Dynamic score calculation based on position-weighted answer selections
- Persistent state management to resume interrupted sessions
- Immediate feedback options based on answer selection
- Result storage with Firebase Firestore for historical comparison
- Cross-device access to previously taken quizzes

### **Scoring System**
- Customizable scoring ranges with percentage-based weighting
- Sophisticated algorithm for calculating decision recommendations
- Interpretive feedback based on configurable score brackets
- Performance metrics visualization with comparative analysis
- Historical tracking of score progression over time
- Exportable score reports for sharing results

### **Cloud Integration**
- Data synchronization between devices
- Backup and restore capabilities
- Real-time updates for collaborative features

---

## **Getting Started**

### **Prerequisites**
- Android Studio (latest stable version recommended)
- JDK 8 or higher
- An Android device (physical or emulator) running Android 5.0 (API 21) or higher
- A Firebase account for backend services

### **Setup**
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/DecisionHelperApp.git
   ```
2. Open the project in Android Studio.
3. Connect your Firebase project:
   - Add your `google-services.json` file to the app directory
   - Ensure Firebase services (Authentication, Firestore, Storage) are enabled
4. Build the project and ensure there are no errors.
5. Run the app on an emulator or physical device.

---

## **Contributing**

1. Fork the project.
2. Create a new branch for your feature/bugfix.
3. Submit a pull request with a detailed explanation of changes.

---

## **License**

This project is licensed under the MIT License. See the `LICENSE` file for more details.
