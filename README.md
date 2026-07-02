# Tour Planner

Git repository: https://github.com/iselinmag/SoftwareEngineering-TourPlanner

Tour Planner is a web app for planning tours and keeping logs of the ones you have done. You can make a tour, see it drawn on a map, and write logs about how each trip went. Every user has their own account, and the app keeps track of who made what.

## Features

You can register an account and log in. Once you are in, you can create tours, each with a name, a description, a starting point, a destination, and a type of transport. The app talks to OpenRouteService to work out the real distance and travel time, and it draws the route on a map using Leaflet.

Tours are shared. Everyone can see every tour and everyone can write a log on any tour. The catch is that you can only edit or delete a tour you made yourself, and you can only edit or delete a log you wrote yourself. This works a bit like restaurant reviews, where anyone can leave one but only the author can change theirs.

Each tour log records the date, a comment, the difficulty, the distance, the time, and a rating from one to five. You can also attach one image to a log. All the images from a tour's logs are pulled together into a small gallery with arrows, so you can flip through the pictures for that tour.

The app also works out two things on its own. Popularity is based on the ratings and the number of logs. Child friendliness is based on how hard the logs say the tour was. You can search across tours and logs by text, and you can import and export your tour data as a file.

## How it is built

The app has two parts that talk to each other over HTTP using JSON.

The backend is built with Java and Spring Boot. It is split into layers, so the controllers handle the web requests, the services hold the logic, and the repositories deal with the database. Data is stored in a PostgreSQL database, and the app uses JPA and Hibernate as the object relational mapper, which means it talks to the database without writing raw queries by hand. Images themselves are saved on the file system, and only the file name is kept in the database. Logging is done with Log4j2.

The frontend is built with Angular and follows the MVVM pattern, so the view models hold the state and the components show it. Logins use a token, which the frontend sends along with every request so the backend knows who you are.

## How to run it

You need Java 21 or newer, Node.js with the Angular CLI, and a running PostgreSQL database.

## Setting it up

First, set up the database. Make a PostgreSQL database for the app. The backend will create the tables for you the first time it starts.

Next, set up the backend config. In the backend resources folder there is a file called application.properties.example. Copy it and rename the copy to application.properties, then fill in your own database name, your database user, your database password, and a secret value for the tokens. This file is kept out of the shared code on purpose, so your private details never get uploaded.

Then start the backend. Open a terminal in the backend folder and run the Maven wrapper to start it. The backend runs on port 8080.

Then start the frontend. Open a second terminal in the frontend folder, install the packages, and start the Angular app. The frontend runs on port 4200. Open that address in your browser and you are ready to go.

## Running the tests

The backend comes with a set of unit tests. To run them, open a terminal in the backend folder and run the test command with the Maven wrapper. The tests do not need the database, because they use stand ins for it.
