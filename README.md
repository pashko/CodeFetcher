# CodeFetcher

### The Task

Build a single screen app that displays 2 pieces of information, a counter for the number of times data has been fetched and the current UUID response code that was fetched, along with a button that can be pressed to fetch a new UUID response code.  Unit tests should be provided for the core functionality.

```
+---------------------------------------------------+
|Response Code: 52a5e208-b75e-4b5c-9f23-fae2287162a6|
+---------------------------------------------------+
|Times Fetched: 12                                  |
+---------------------------------------------------+

               +----------------------+
               |    Fetch Code        |
               |                      |
               +----------------------+
```


Each time the Fetch code button is pressed, the app should fetch a new response code from the backend server and display the code (see Fetching Data for the server API).  The "Times Fetched" count should show the number of times that the data has been fetched in the lifetime of the app, persisting the count through app restarts.


### Fetching Data

The mock server is provided for both python and python 3. You can run the backend server (located in the same zip) by running "python server.py" or "python3 server3.py".  This will make the server available on http://localhost:8000.  To fetch data, first request the root page (http://localhost:8000) to get the correct path:

This will return a json object with one key "next_path", which details the endpoint to retrieve the response code from (note the next_path changes with every request).  An example response is:
{"next_path": "http://localhost:8000/d/"}

The app can then fetch the url provided in "next_path" and retrieve the response code, An example response is:
{"path": "d","response_code": "8f26843d-581d-40cb-9e81-aeccd9727902"}.  It is impertive that the url provided in next_path is used to generate the response code only once and this will be enforced when the product goes into production (This functionality has not been built into the mock server).

If the app attempts to request a "next_path" value that isn't the last one to be returned by the server, the server will respond with an error:
{"error": "App requested the wrong path, expected: d"}

The server will print the current "next_path" and "response_code" values to the console, which can be used to confirm the app is performing correctly.

### What we're looking for

A working Android implementation of the screen, detailing any assumptions that were made during the build.

Implementations in Rx are preferred over solutions that don't use it.  The task is simple to achieve but we're looking for code architecturally split to allow for high test coverage, preferably using MVI or MVVM.  There are very few parts of the project that cannot be unit tested.
