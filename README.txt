Smarsh Cache - Web Content Retriever
====================================

What it does
------------
You give the program a URL. It gets the page content from the web
and saves it to a local file. The next time you ask for the same URL,
it reads the content from the local file instead of the web.

So each URL is fetched from the web only once.

For every run it prints:
- the date when the content was first fetched from the web
- the URL
- the page content


Java version
------------
This program needs Java 25.

It uses newer Java 25 features (the simple "void main()" entry point
and the built-in IO class), so an older Java will not compile it.
The Java version is also pinned in the pom.xml.


How to build and run
--------------------
Build and run the tests:
    mvn clean test

Run the program:
    mvn compile exec:exec   (or run ApplicationMain from your IDE)

When it starts, type a URL and press Enter.

Note: use "exec:exec", not "exec:java". This app uses the Java 25
instance "void main()", which only the real "java" launcher can run.


Caching approach
----------------
The cache is kept simple: it is just files in a local folder (".cache").

- Each URL is turned into a SHA-256 hash. That hash is the file name.
  This keeps file names safe and unique, with no special characters.

- Two files are written per URL:
  1. <hash>          - the saved entry (content + the original fetch date).
                       This is the file the program reads on the next run.
  2. <hash>.content  - the raw page content, saved exactly as received.
                       This one is only for you to look at when debugging.

- If a saved file is missing or unreadable, the program just fetches
  the page again from the web.

There is no database and no expiry time. Once a page is saved, that
saved copy is used from then on. This is on purpose, to keep it simple.


Assumptions
-----------
- If you type a URL with no scheme (like "example.com"), "https://" is
  added for you.
- The scheme and host are lowercased so that the same page is not saved
  twice (for example "HTTP://Example.com" and "http://example.com").
  The path and query are left as-is, because they can be case sensitive.
- Page content is treated as text when printed.
- The cache folder ".cache" is created in the folder you run from.
- A page is only saved when the web request succeeds (a normal 2xx
  response). Errors like 404 or 500 are not saved.
