# SocketModeApp

Slack app which is built on Android, there are 2 functionality that is implemneted now : 
1. When a user executes `/prajna-command` it posts a message with `:wave: Hello!`
2. On any given message when a user reacts with an emoji :white_check_mark: it posts a thread message. This is done by listening to the reaction added event through socket mode.
