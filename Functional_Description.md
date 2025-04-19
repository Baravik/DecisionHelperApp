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
- Primary action buttons for key app functions

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
   - Adding images to questions for better context and visualization
   - Real-time preview of how questions will appear

3. **Scoring Configuration**:
   - Setting up scoring ranges for results interpretation
   - Creating feedback texts for different score brackets

### Taking Quizzes

Users can access and complete quizzes through:

1. **Quiz Selection**:
   - Browsing available public quizzes
   - Accessing previously created personal quizzes
   - Viewing quizzes shared specifically with them

2. **Quiz Completion Process**:
   - Sequential navigation through quiz questions
   - Selection of most appropriate answers
   - Progress tracking during quiz completion
   - Option to save progress and resume later

3. **Results and Recommendations**:
   - Receiving calculated scores based on answer weights
   - Viewing personalized recommendations based on score ranges
   - Option to save results for future reference

### Score Management

Users can track and analyze their decision-making patterns through:

1. **Results History**:
   - Viewing historical quiz results organized chronologically
   - Filtering results by quiz type or date range
   - Comparing results across multiple quizzes

2. **Performance Analysis**:
   - Visualizing decision patterns over time
   - Identifying consistent preferences or changes in approach

### User Profile Management

Users can manage their account settings through:

1. **Profile Information**:
   - Updating personal information (name, email)
   - Changing profile picture
   - Viewing account statistics (quizzes created, quizzes taken)

2. **Account Settings**:
   - Password management (for email users)
   - Account deletion options
   - Authentication method management

## Example Use Cases

### Scenario 1: Career Decision

A user facing multiple job offers could create a decision quiz with questions like:

- "How important is salary in your decision?" (with weighted options)
- "How would you rate the company culture?" (with image attachments of offices)
- "What commute time would you prefer?" (with different time range options)

After completion, the app calculates which job offer best aligns with the user's weighted preferences.

### Scenario 2: Purchase Decision

A user deciding between different product options could:

- Create a quiz with specific feature comparisons
- Add images of products being considered
- Set higher weights for must-have features
- Share the quiz with family members for additional input

The final score helps identify which product best matches their needs based on their personal criteria weighting.

### Scenario 3: Life Choices

For significant life decisions (relocating, education choices), users can:

- Build comprehensive questionnaires covering multiple factors
- Save partially completed quizzes while gathering information
- Review historical responses to similar decisions
- Track how their priorities have changed over time

## Benefits

- **Structured Approach**: Transforms complex decisions into manageable components
- **Personalization**: Adjusts to individual priorities through weighted scoring
- **Visualization**: Provides clear visual representation of options and outcomes
- **History Tracking**: Enables review of past decisions and their reasoning
- **Collaboration**: Allows sharing decision frameworks with others for input
- **Cross-Device Access**: Syncs decision data across user devices
