Feature: Tracking and cloud save
  Background:
    Given User is logged in as account@gmail.com
  Scenario: Tracking listens (1)
    When User starts playing a track in a media player
    Then The app should log the event
    And  The track should appear in history marked as "Now Playing"
  Scenario: Tracking listens (2)
    When User pauses the track
    Then The app should log the event
  Scenario: Tracking listens (3)
    When User skips ahead or forward in the track
    Then The app should log the event
  Scenario: Tracking listens (4)
    When User stops the track or closes the media player
    Then The app should log the event
  Scenario: Tracking listens (5)
    Given User stops the track or closes the media player
    When  User has listened to the track for more than 4 minutes or half its length
    Then  The track should appear in user's history
  Scenario: Tracking listens (6)
    Given User stops the track or closes the media player
    When  User has listened to the track for less than 4 minutes or half its length
    Then  The track should not appear in user's history
    And   The track's skip count should increase by 1
  Scenario: Cloud save (1)
    Given The app logs an event
    When  The user has Internet access
    Then  The app should store the event in the Firestore database linked to account@gmail.com
  Scenario: Cloud save (2)
    Given The app logs an event
    When  The user doesn't have Internet access
    Then  The app should store the event in the queue
  Scenario: Cloud save (3)
    Given The event queue is not empty
    When  The user goes online
    Then  The app should submit all events in the queue to the Firestore database linked to account@gmail.com
  Scenario: Sync across devices
    Given The user opens the app
    When  There are unprocessed events in the Firestore database
    Then  The app should fetch all missing events and process them