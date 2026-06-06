# Claude Code Architect prompts

## Tests / Common AI prompt
The unit test coverage should be always at 100%. If the test coverage is not at 100% - that usually means that the test is not covering all the code paths, there is dead code or worse there is a bug. Please make sure that the test coverage is at 100% for:
- the new code you are writing
- the existing code you are modifying

## Project setup
### Description:
I would like to create a Kotlin project to discover user's IP address. Obviously if user is using VPN the IP address from that VPN should be shown. I would like to see also the city name, timezone and the map. From that project we should be able to create iOS app, iPad OS, MacOS, Android app, Web app, Linux app and Windows app if possible. THe common business logic should be re-used, but if the platform specific code needed, it should be done in separate directory. I would like to create unit tests in a separate directory. That is the first time I am trying to create this kind of project. So lets brainstorm hwo the directory structure should be create to accomplish the task described above.
Please create a plan how to create the project and the best approach to create directory structure.

## Common:
Also please use the dev-guidance: /Users/abk/.claude/skills/dev-guidelines/SKILL.md when creating a plan
