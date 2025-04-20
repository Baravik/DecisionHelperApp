# Decision Helper App: Functional Description

## Overview

Decision Helper App is an Android application designed to help users make informed decisions through customizable questionnaires. The app provides a structured approach to decision-making by allowing users to create quizzes with weighted questions and answers, share these with others, and receive tailored recommendations based on their inputs.

## User Journey

### Authentication

1. **Initial Access**: Users start with a splash screen that automatically redirects to the login screen or main screen based on previous authentication status.
2. **Login Options**:
   - Email and password authentication with validation
   - Google Sign-In integration for quicker access
   - New user registration with required fields for name, email, and password

### Main Dashboard

After successful authentication, users are presented with the main dashboard that offers:

- Personalized welcome with user name and profile picture (if available)
- Navigation to user profile for account management
- Primary action buttons for key app functions:
  - Start or browse quizzes
  - View personal score history
  - Create new quizzes

### Creating Decision Quizzes

Users can create customized decision-making quizzes through:

1. **Quiz Setup**:
   - Creating a title for the decision quiz
   - Adding a description of the decision scenario
   - Setting privacy options (public/private)

2. **Question Management**:
   - Adding multiple-choice or yes/no questions relevant to the decision
   - Customizing answer options with percentage-based importance weighting
   - Reordering questions and answer options via drag-and-drop

3. **Scoring Configuration**:
   - Setting up scoring ranges for results interpretation

### Taking Quizzes

Users can access and complete quizzes through:

1. **Quiz Selection**:
   - Browsing available public quizzes
   - Accessing previously created personal quizzes
   - Option to delete quizzes marked as personal

2. **Quiz Completion Process**:
   - Sequential navigation through quiz questions
   - Selection of most appropriate answers
   - Progress tracking during quiz completion
   - Ability to navigate back to previous questions and review selected answers
   - Option to change previously submitted answers before final submission

### Score Management

Users can track their decision-making patterns through:

 **Results**:
   - Receiving calculated scores based on answer weights
   - Option to save results for future reference
   - Option to delete quiz scores
     
### User Profile Management

Users can manage their account settings through:

1. **Profile Information**:
   - Changing profile picture
   - Viewing account details

2. **Account Settings**:
   - Password management (for email users)
   - Account deletion options
   - Authentication method management
   - Connecting additional sign-in methods (e.g., linking Google account)

## Example Use Cases

### Scenario 1: Career Decision

A user facing multiple job offers could create a decision quiz with questions like:

- "How important is salary in your decision?" (with weighted options)
- "How would you rate the company culture?" (with weighted options)
- "What commute time would you prefer?" (with different time range options)

After completion, the app calculates which job offer best aligns with the user's weighted preferences.

### Scenario 2: Purchase Decision

A user deciding between different product options could:

- Create a quiz with specific feature comparisons
- Set higher weights for must-have features
- Share the quiz for additional input

The final score helps identify which product best matches their needs based on their personal criteria weighting.

### Scenario 3: Life Choices

For significant life decisions (relocating, education choices), users can:

- Build comprehensive questionnaires covering multiple factors
- Save partially completed quizzes while gathering information
- Review historical responses to similar decisions
- Track how their priorities have changed over time

