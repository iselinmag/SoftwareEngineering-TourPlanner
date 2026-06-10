# Tour Planner

A web application for planning tours and keeping track of the ones you actually finished. A user can create bike, hike, running or vacation tours, see them drawn on a map, and write logs about how each tour went. This was built as a semester project for our software engineering course.

The app is split into a Spring Boot backend and an Angular frontend that talk to each other over HTTP. Tour data lives in a PostgreSQL database, and the images you upload for a tour are kept on the filesystem.

## What it can do

You start by registering an account and logging in. Everything you create afterwards belongs only to you, so there is no sharing between users.

Once you are in, you can create tours. A tour has a name, a description, a start and an end point, a transport type, plus the distance, the estimated time and a map. The distance and time are not typed in by hand. The app sends the start and end to the OpenRouteService API and gets those values back, and the route itself is drawn on a Leaflet map. All of your tours sit in a list where you can create, edit and delete them.

For each tour you can add tour logs. A log is basically a record of one time you did the tour, so it holds a date and time, a comment, a difficulty, the total distance, the total time and a rating. One tour can have many logs, and just like the tours, the logs can be created, edited and deleted.

On top of what you enter, the app works out a couple of things on its own. Popularity comes from how many logs a tour has, and child friendliness is estimated from the difficulty, time and distance found in those logs.

There is a full text search across both tours and logs, and it also looks inside the computed values, so searching reaches more than just the plain text fields. You can also export your tour data to a file and import it back later.

Our unique feature is an image gallery. Every tour can have its own images that the user uploads, and all of them are collected into one shared image section so you can browse them in one place.

## How it is put together

The backend follows a layered design so each part has a clear job. The presentation layer holds the REST controllers that the frontend calls. Below that sits the business layer with the actual logic, like the search, the computed attributes and the calls out to OpenRouteService. At the bottom is the data access layer that talks to PostgreSQL through the OR mapper (a library that turns database rows into Java objects so we don't write raw SQL by hand). A layer only ever calls the one directly beneath it, and each layer carries its own exceptions instead of leaking database specific errors upward.

On the frontend we use Angular and follow the MVVM pattern, which keeps the view and the data behind it nicely separated so the screen updates on its own when the underlying values change. We also built our own reusable UI component so the same piece can be dropped in wherever we need it.

For the design pattern requirement we relied on [name your pattern here, for example the Repository pattern in the DAL or an Observer in the frontend], which we describe in more detail in the protocol.

## Tech stack

The backend runs on Java with Spring Boot and uses JPA together with Hibernate as the OR mapper. Data is stored in PostgreSQL. Logging goes through log4j, and the tests are written with JUnit. The frontend is built with Angular and TypeScript, with Leaflet handling the maps. Routing data comes from the OpenRouteService.org Directions API.

## Getting started

You will need Java 17 or newer, Maven, Node.js with npm, and a running PostgreSQL instance. You also need a free API key from OpenRouteService.org so the route lookups work.

First set up the database. Create an empty PostgreSQL database for the project and remember the connection details, since you will put them into the configuration in a moment.

None of the secrets or connection settings live in the source code. They are kept in a separate configuration file so the repository stays clean. Copy the example config and fill in your own values:

```
cp backend/src/main/resources/application.example.properties backend/src/main/resources/application.properties
```

Inside that file set your database url, username and password, your OpenRouteService API key, and the base directory where uploaded tour images should be saved.

To run the backend, go into the backend folder and start it with Maven:

```
cd backend
mvn spring-boot:run
```

To run the frontend, open a second terminal, install the dependencies once and then start the dev server:

```
cd frontend
npm install
ng serve
```

The frontend is then reachable at http://localhost:4200 and it expects the backend to be available on its usual port.

## Running the tests

We wrote over twenty unit tests to cover the parts of the code that matter most, like the computed attributes, the search and the data access. You can run all of them from the backend folder:

```
cd backend
mvn test
```

## Project layout

The repository is divided into two main folders. The backend folder holds the Spring Boot application with the controllers, the business logic, the data access layer and the tests. The frontend folder holds the Angular application with the components, services and view models. The protocol with the diagrams, wireframes and our notes is kept in the docs folder.

## Team

This project was created by [your name] and [teammate's name].
