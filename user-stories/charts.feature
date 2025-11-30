Feature: Charts
  Background:
    Given User has logged in as account@gmail.com
  Scenario: Basic charts
    When User clicks on the "Charts" button in the botom bar
    Then The user should see a list of most listened artists, albums or tracks
    And  Charts should be sorted by time listened
    And  User can change between the charts by swiping
  Scenario: Changing the sorting of charts (1)
    Given User is seeing the charts
    When  User clicks the "Sort by time listened" button
    Then  Charts should be sorted by amount of plays
  Scenario: Changing the sorting of charts (2)
    Given User is seeing the charts
    And   Charts are sorted by amount of plays
    When  User clicks the "Sort by plays" button
    Then  Charts should be sorted by time listened
  Scenario: Clicking on a track
    Given User is seeing the track charts
    When  User clicks on one of the tracks
    Then  Take the user to the details page for that track
  Scenario: Clicking on an album
    Given User is seeing the album charts
    When  User clicks on one of the album
    Then  Take the user to the details page for that album
  Scenario: Clicking on an artist
    Given User is seeing the artist charts
    When  User clicks on one of the artists
    Then  Take the user to the details page for that artist