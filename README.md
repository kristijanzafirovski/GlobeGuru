# GlobeGuru

> This project was developed as a **FINKI course project** (Faculty of Computer Science and Engineering, Ss. Cyril and Methodius University in Skopje).

GlobeGuru is a travel search tool. A user enters a destination, departure date and number of travellers in a web form, and the backend scrapes configured travel agency websites in parallel and returns the matching offers (hotel name, country, price and link).

## How it works

1. `ScraperServer` starts a Java HTTP server on port **8000**. It serves the static front end and exposes a `/submit` endpoint.
2. The front end posts the search form as JSON to `/submit`.
3. A `Scraper` thread is started for the search. It reads the list of agency URLs from `URLsJSON.json` and runs one `ScraperThread` per site.
4. Each thread loads the page with **Selenium** (headless Chrome) and parses the rendered HTML with **jsoup**.
5. The collected `Option` objects are serialised with Jackson and returned to the page, which renders them as result cards.

Agencies are configured in `backend/GlobeGuru-backend/src/main/java/URLsJSON.json`, so new sources can be added without changing the code.

## Getting started

### Prerequisites

- Java
- Maven
- Google Chrome installed on the machine (Selenium uses it in headless mode)

### Run

```bash
cd backend/GlobeGuru-backend
mvn package
```

Then start `ScraperServer` (the `main` class) from your IDE, or run it with the Maven dependencies on the classpath. The server must be started from the `backend/GlobeGuru-backend` directory, because the frontend files are loaded from a relative `frontend/` path.

Once it is running, open http://localhost:8000 in your browser.

## Project structure

```
GlobeGuru/
├── backend/
│   └── GlobeGuru-backend/
│       ├── frontend/            # HTML, CSS and JavaScript served by the backend
│       ├── pom.xml
│       └── src/main/java/       # ScraperServer, Scraper, ScraperThread, Option
└── frontend/
    └── GlobeGuru-frontend/      # Additional front-end files (public/index.html)
```

## Tech stack

Java · Selenium · jsoup · Jackson · Java `HttpServer` · HTML / CSS / JavaScript

## Notes

Web scraping depends on the structure of each agency's website. If a site changes its layout, the selectors in `ScraperThread` will need updating. Check each site's terms of use before scraping it.

## Author

**Kristijan Zafirovski**
