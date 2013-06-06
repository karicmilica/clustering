# clustering

An application written in Clojure using Noir, Enlive, data.json and Monger libraries.

Main functionalities:
 1. filling database with the recipes
 2. divide recipes into groups

## Usage

It's necessary to start MongoDB before running the application. Database used in this project is MongoDB 2.2.3 
(to download, visit http://www.mongodb.org/downloads). To start database open command line, navigate to mongodb/bin
folder, and then execute mongod.exe (on windows). For more detailed instructions on how to start MongoDB,
see http://docs.mongodb.org/manual/installation/.

To start application, open command line, navigate to the application folder and then: 
- to fill database with recipes, set :main to **clustering.scraper.recipe_extractor** in project.clj and
  launch lein run from the command line
- to divide recipes into groups set :main to **clustering.kmeans in project.clj** and launch *lein run* 
  from the command line
- to start web-application set :main to **clustering.server.server** in project.clj, launch *lein run* 
  from the command line and type http://localhost:8080/recipes in your browser address bar


## License

Distributed under the Eclipse Public License, the same as Clojure.
