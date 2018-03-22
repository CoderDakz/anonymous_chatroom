README.TXT
------------------------------------------------------
Name: Alexander Sadakhom


LIMIT TRACEABLE TO INDENTIFIABLE INFORMATION STRATEGY
-------------------------
-Login window allows user to enter their username of choice to be placed on their client and potentially broadcasted alongside their messages
-Login window has defensive coding vs empty strings, names with spaces, & names with more than 8 characters in length.
-There is a toggle anonymity button on the login window.

USER CONTROLLED ANONYMITY STRATEGY
-------------------------
- The toggle anonymity button on the login window allows users to decide whether or not they want their messages to be broadcasted alongside their names or if they want to remain anonymous.
- The button is connected to an integer value. If the integer value is = 0 then the user isn't anonymous, if it is = 1 then they are.

MESSAGER SERVER STRATEGY
-------------------------
-Designed the chatroom using the free google database called 'firebase'.
-It didn't run natively on java so I had to find a free java wrapper online at https://github.com/bane73/firebase4j
-My strategy was to take the string that a user wrote into the text entry field, send that message to the database.
-Every time a message is sent to the server, it increments a variable, "message", that is stored in the database. 
-Main function runs an automated timer every .5 of a second, to check if the "message" is greater than 0. If so, it clears the chat box, reprints the entire chat server and resets "message" to be 0.

 
-Message server also includes timestamps on the messages, & includes a counter on everyone's client that shows the number of currently active users in the chatroom.

COMPILATION
------------------
Build & clean inside of netbeans IDE & creates a JAR file inside of the DIST folder.

EXECUTION
------------------
Inside of the assignment folder, go into the DIST subdirectory and double click the JAR file to run the program.

To run from the command line, CD into the assignment directory, cd into the dist subdirectory and type java -jar “cis4110a1.jar”

EXITING
————
User must exit by clicking “cancel” in the login window GUI
or by clicking “quit” in the main client gui,. If done any other way it may 
alter the integrity of the “Active User” count in the database.