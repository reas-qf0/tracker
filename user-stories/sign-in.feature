Feature: Sign In
  Scenario: User is not signed in
    Given User is not signed in
    When  User opens the app
    Then  The app should not be usable
    And   A prompt asking the user to sign in should appear
  Scenario: User clicks the "Sign in" button
    Given User is not signed in
    When  User clicks the "Sign in" button
    Then  A list of user's Google accounts should appear
  Scenario: User logs in
    Given User has opened the accounts list
    When  User clicks on one of the Google accounts
    Then  The main application should open
    And   The user must be logged in as the account he chose