[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/jIcy9eHZ)
# FC 008
Groups for this challange are the same as groups for FC_006 as this challenge is an extension of FC_006.
This challenge can be worked on in pairs of 2 and the focus for this challenge is on the topics we covered this week:
1. Fetching API's
2. Loading State
3. Retrofit

## Github Classrooms
We will be using Github classroom to do in-class Friday Challenges.
You can make as many commits and as many pushes as needed to the main branch on your forked copy of the repo.
The notes about commits are still relevant here:
To be consistent, use the same styling for commit messages that was given in the Project I:
- PREFIX – Short description of the change
  A detailed description can be added to the commit in the long description, if needed.
  The following are the possible options for [Prefix]
- [FEAT] - For new features or major additions to the project.
  FEAT - Added button click-ability feature 
- [FIX] - For bug fixes, corrections, or revisions to the code.
  FIX - Corrected navigation bar alignment on mobile devices
- [STYLE] - For stylistic changes such as formatting, CSS modifications, or minor visual updates.
  STYLE - Updated color scheme for better contrast
- [DOCS] - For changes or additions to the documentation, including README files and comments in the code.
  DOCS - Added project description and setup instructions to README
- [SECURITY] - For changes related to improving the security of the website.
  SECURITY - Implemented input validation for contact form
- [REFACTOR] - For code refactoring that doesn’t change functionality but improves code quality or organization.
  REFACTOR - Organized attributes for button components files in Home layout.
- [TEST] - For adding tests or making changes to the testing suite.
  TEST - Added validation tests for login form input

### 5. Submitting your work
Once, you are sure that all the work is completed, go through the following steps for submission.
Push all your work onto the main branch. **Only the main branch** will be considered for grading.

## Project description
Your application will function as a small Asteroid Monitoring Console used by mission control.
Before you start working on the activity, make sure to create an API key from this website - https://www.api.nasa.gov.

Scientists track Near-Earth Objects (NEOs) - asteroids that pass close to Earth’s orbit. 
Your application must connect to a NASA web service and retrieve information about these objects one at a time.

The goal of this challenge is to build a simple interface that:
* Sends a request to the NASA NEO API 
* Retrieves asteroid data 
* Displays important information about one asteroid (at a time)
* Extra points for creating visually appealing frontend.
* Handles loading and error states

Total points - 46

Task 1 - 3

Task 2 - 5

Task 3 - 10

Task 4 - 10

Task 5 - 10

Task 6 - 5

Bonus  +5

## Re-use your Code from FC_006 
Re-use the code from FC_006 and continue working on that.
### Task 1 – Setup Notification System
The relevant code for setting up the notification chanenls and sending notifications are present 
on the slides.
* Create a notification channel
* Request notification permission (Android 13+)
* Refer to lecture slides for setup

###  Task 2 – Create Alert Triggers
Your app must include at least 2 different triggers for notifications.

Examples:
* Button click (use a delay timer to emulate some time passing) -> “Scan Complete”
* Timer notification (to emulate a signal coming from oustide) -> “Incoming Signal Detected”
* Random event (generate a random event notification) -> “Asteroid Nearby”

###  Task 3 – Design Notification Content

Each notification must include:
* Title
* Message
* Icon (optional for bobus points, more details at the end)

Content should:
* Be clear
* Be meaningful
* Match the theme (Mission Control / Alerts)



###  Task 4 – Multiple Notification Types

Create at least two different types of notifications, such as:
* Informational (e.g., “Scan Complete”)
* Warning (e.g., “Hazard Detected”)
* Evaded (e.g.,  successfully evaded a threat)

###  Task 5 – User Interaction

When user taps the notification:
* Open the app
* OR navigate to a relevant screen

###  Task 6 – Timing / Behavior
Notifications should not all be instant.

At least one notification must:
* Be delayed (e.g., 5–10 seconds)
* OR triggered after some condition

### Expected Behavior
* Notifications appear correctly
* Different triggers produce different alerts
* Notifications feel intentional, not random
* App handles permission properly

### Constraints
* Do not spam notifications continuously
* Do not crash if permission is denied, make sure to add proper error handling logic.
* Use proper notification channels

### Bonus +5 (upto) points for:
* Creative themes (space, game, system alerts, etc.)
* Multiple notification types
* Good UI/UX design
* Adding notification specific icons
* Smooth user experience


