# The RSO Manifesto Bot!
The new version of the RSO Manifesto Bot is out now! The bot is written in Java using JDA, and the website is written also in Java using Spring Boot. For now, the website will be staying close-source, but I will make it open to the public eventually.

## Features
* !manifesto command
  * Saves the previous message into the database
  * Prevents duplicate manifestos
  * Prevents self-manifestos
  * Prevents manifestos of bot messages
  * Prevents manifestos of words on the manifesto blacklist
* !randommanifesto command
* !getmanifesto <id> command
* !manifestohelp command
* Note: !randommanifesto and !getmanifesto share a one minute limit to send a manifesto to prevent spam
* Manifesto reaction deleting
  * Every manifesto sent will have a 🗑️ reaction automatically added. Once 10 people also add the reaction, the manifesto will be deleted from both chat and the database.
* Website (https://rso-manifesto-website.herokuapp.com/)
  * This website shows all of the current manifestos and also allows for searching via either ID, manifesto content, or manifestoer (the person who issued the !manifesto command).

That's about it for now. Should there be any problems or suggestions, please feel free to contact me on Discord @ JazzyJake#5032. Cheers!
