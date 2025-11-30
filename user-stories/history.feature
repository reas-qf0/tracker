Feature: History view
  Background:
    Given User has logged in as account@gmail.com
  Scenario: View history
    When User has opened the app
    Then A list of recently played tracks by account@gmail.com should appear
  Scenario: View history via navigation bar
    When User has clicked the "History" button on the bottom bar
    Then A list of recently played tracks by account@gmail.com should appear
  Scenario: User opens options
    Given User is looking at his play history
    When  User has clicked on the "More" button near one of the entries
    Then  An options list should appear for that entry
  Scenario: Editing entries (1)
    Given User has clicked on the "More" button near one of the entries
    When  User has selected "Edit"
    Then  An "Edit Entry" screen should appear
    And   The entry's information should be automatically filled in
  Scenario: Editing entries (2)
    Given User has clicked on the "More" button near one of the entries
    And   User has selected "Edit"
    When  User has clicked "OK"
    Then  The entry's information should be replaced with new input
    And   User should return to the History screen
  Scenario: Editing entries (3)
    Given User has clicked on the "More" button near one of the entries
    And   User has selected "Edit"
    When  User has clicked "Cancel"
    Then  The entry's information should remain intact
    And   User should return to the History screen
  Scenario: Deleting entries (1)
    Given User has clicked on the "More" button near one of the entries
    When  User has selected "Delete"
    Then  A confirmation popup should appear
  Scenario: Deleting entries (2)
    Given User has clicked on the "More" button near one of the entries
    And   User has selected "Delete"
    When  User clicks "OK" in the confirmation popup
    Then  The entry should be removed from the history list
  Scenario: Deleting entries (3)
    Given User has clicked on the "More" button near one of the entries
    And   User has selected "Delete"
    When  User clicks "Cancel" in the confirmation popup
    Then  The entry should not be removed from the history list
  Scenario: Clicking on the entry
    Given User is looking at his play history
    When  User clicks on one of the entries
    Then  A popup with basic information about the artist, track, and album should appear
  Scenario: Open the track page
    Given User is looking at a basic information popup
    When  User clicks on the "More info" button next to the track name
    Then  Take the user to the details page for that track
  Scenario: Open the album page
    Given User is looking at a basic information popup
    When  User clicks on the "More info" button next to the album name
    Then  Take the user to the details page for that album
  Scenario: Open the artist page
    Given User is looking at a basic information popup
    When  User clicks on the "More info" button next to the artist name
    Then  Take the user to the details page for that artist
  Scenario: Open track history
    Given User is looking at a basic information popup
    When  User clicks on the amount of plays next to the track name
    Then  Take the user to the list of plays for that track