Feature: Ensure that the "Computer Sciences" research fields exist

    Background:
        * url baseUrl
        # Research fields do not have an endpoint (yet)
        * def researchFieldBase = '/api/resources/'

    Scenario: Obtain the research field by its ID

        Given path researchFieldBase + 'R132'
        When method GET
        Then status 200
