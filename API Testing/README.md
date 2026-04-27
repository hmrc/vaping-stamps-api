# Bruno API Testing

This directory contains [Bruno](https://www.usebruno.com/) collections for the Vaping Stamps API ecosystem.

> **Important**: These collections are intended as developer and tester aids during active development. They provide example API calls and simple tests for manual verification. These tests are **not included in CI/CD** and may become out of sync with the codebase if not manually updated. The plan is to remove these collections before handover to live services.

## Directory Structure

- **`vaping-stamps-api/`**: Example calls and simple tests for the submission service API endpoints.
- **`environments/`**: Configuration for `local`, `staging`, `qa`, and `externaltest`.

## Running Tests

### Local Environment Only

**Tests should be run locally.** Running the full test suites against deployed environments is not recommended because:


### Using the Bruno GUI

Most of the team uses the [Bruno desktop application](https://www.usebruno.com/downloads) to run these collections:

1. Download and install Bruno from https://www.usebruno.com/downloads
2. Open Bruno and select "Open Collection"
3. Update .env file with in `API Testing` folder and update it with real values
4. Navigate to the `API Testing` folder and open any of the collection folders
5. Select the environment from the environment dropdown
6. Run individual requests or use "Run Folder" to execute multiple tests


