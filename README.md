
# DecisionHelperApp

DecisionHelperApp is an Android application designed to help users make informed decisions through customizable questionnaires. Users can answer questions, rate their importance, and receive tailored recommendations based on their input.

---

## **Features**
- Create and answer quizzes to evaluate decisions.
- Rate the importance of questions for personalized results.
- Save or discard quiz results.
- Admin panel for approving or rejecting new quizzes and questions.

---

## **Technologies Used**

1. **Android (Java)**:  
   The entire application is built in Java within Android Studio. The Android framework handles the UI, lifecycle, and logic of the app, while ensuring compatibility with Android devices.

2. **XML**:  
   Used for defining the layout and design of the app's UI.  
   - Example: `activity_main.xml` defines the main menu layout, including buttons and text.

3. **JDBC**:  
   Used to establish communication with the SQLite database. JDBC facilitates querying, inserting, updating, and deleting data.

4. **SQLite**:  
   A lightweight database embedded within the app, used to store user data, quiz details, questions, ratings, and admin-related information.

5. **Web Services (Optional)**:  
   If extended to support centralized data, Spring Boot REST APIs can be used for cloud integration to manage quizzes and users centrally.

---

## **Architecture: MVVM (Model-View-ViewModel)**

DecisionHelperApp follows the **MVVM architecture** to ensure a clear separation of concerns, modularity, and ease of maintenance.

### **Model**  
The **Model** layer represents the data and business logic of the application. It is responsible for handling data operations, such as fetching data from the database or network.  

Files in this layer:
- `models/`
  - `Quiz.java`: Represents a quiz.
  - `Question.java`: Represents a question.
  - `User.java`: Represents a user in the system.
- `database/`
  - `DatabaseHelper.java`: Handles database creation and schema.
  - `QuizDAO.java`, `QuestionDAO.java`, `RatingDAO.java`: Manage CRUD operations on quizzes, questions, and ratings.

---

### **View**  
The **View** layer is responsible for the UI and user interaction. It is implemented using XML layouts and Activities in the app.  

Files in this layer:
- **XML Layouts** (res/layout/):
  - `activity_main.xml`: Main menu layout.
  - `activity_quiz.xml`: Quiz answering layout.
  - `activity_rating.xml`: Question rating layout.
  - `activity_admin.xml`: Admin panel layout.

- **Activities** (activities/):
  - `MainActivity.java`: Handles user navigation from the main menu.
  - `QuizActivity.java`: Displays quizzes for answering.
  - `RatingActivity.java`: Allows users to rate question importance.
  - `AdminActivity.java`: Provides admin functionalities.

---

### **ViewModel**  
The **ViewModel** layer bridges the View and Model layers. It processes data from the Model and prepares it for the View. It also contains logic for managing UI-related data in a lifecycle-aware manner.  

Files in this layer:
- **Adapters** (adapters/):  
  Handle the binding of data to Views in dynamic UI components like RecyclerViews.
  - `QuizAdapter.java`: Manages the display of quizzes in a list.

- **Utilities** (utils/):  
  - `ApiService.java`: Handles communication with Web Services (if used).
  - `Utils.java`: Contains helper methods for processing and formatting data.
  - `Constants.java`: Stores static configuration and constants.

---

## **How It Works**

1. **Quizzes and Questions**:
   - Users can select or create quizzes.
   - Questions are rated for importance and answered to compute a "recommendation score."

2. **Admin Panel**:
   - Allows an admin to approve or reject new quizzes and questions.

3. **Database Operations**:
   - All user data, ratings, and quiz results are stored locally in SQLite via JDBC.

---

## **Getting Started**

### **Prerequisites**
- Android Studio installed on your system.
- Basic knowledge of Java and Android development.
- A physical or virtual Android device for testing.

### **Setup**
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/DecisionHelperApp.git
   ```
2. Open the project in Android Studio.
3. Build the project and ensure there are no errors.
4. Run the app on an emulator or physical device.

---

## **Contributing**

1. Fork the project.
2. Create a new branch for your feature/bugfix.
3. Submit a pull request with a detailed explanation of changes.

---

## **License**

This project is licensed under the MIT License. See the `LICENSE` file for more details.
