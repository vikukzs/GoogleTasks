# GoogleTasks
implementation of https://developers.google.com/google-apps/tasks/quickstart/android for android that works form my in most basic scenario

How to run it

as writen (https://developers.google.com/google-apps/tasks/quickstart/android) but in short:

for default from androidStudio
keytool -exportcert -keystore ./.android/debug.keystore -list -v

password is "android"

go to https://console.developers.google.com

paste SHA-1 key to site https://console.developers.google.com

in package name type "com.example.quickstart"

debug/run app

give permissions

click "CALL GOOGLE TASKS API"
