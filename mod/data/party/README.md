This directory contains Hypixel's party messages for all 25 languages.

It has been collected by running
`/dgdebug partycollection (secondary ign) (effective fragbot) (offline player)`

- Secondary IGN is the ign you must have control of
- In the implementation, http server is running on localhost:3000 which makes Secondary IGN player send the body as a chat message
- FragBot is the bot which auto accepts party invites from secondary ign
- Offline player is player who exists, but is not on hypixel.

This will create file in runtime/partymessages.txt


The file has been post-processed, and included as resource to dg.

