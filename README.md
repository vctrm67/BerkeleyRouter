# BerkeleyRouter
#### I develop the back end of an application that allows one to dynamically route directions in the Berkeley area. The program parses available street data in OSM XML format and uses it for interactive display.
Acknowledgements: UC Berkeley CS 61B (Data Structures and Algorithms) for providing the front end. 

## Features (I built): 
**1. Dynamic Image Rastering**
  * Reconfigures images pulled from local host based on degree of zoom and window size. 

**2. Routing**
  * Finds the shortest path between any two points, taking into account drivable roads, using the A* search algorithm. 
  * Displays step-by-step driving directions in a side window.
  
**3. Search** 
  * Locates and displays points of interest on the map corresponding to the search query. 
  
**4. Autocomplete With Search**
  * Dynamically autocompletes search queries with potential points of interest, implemented through trie data structures. 
  
## To Run: 
  1. Download the directory
  2. Run the file `MapServer.java` and open `localhost:4567` in browser. 

