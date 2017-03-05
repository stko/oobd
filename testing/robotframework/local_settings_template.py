OobdHost="steffen-Desktop64.fritz.box"

OobdHttpPort="8080"

OobdWsPort="8443"

OobdTelnetHost="steffen-Desktop64.fritz.box"

OobdTelnetPort="8081"
FirmwareHost="localhost"
FirmwarePort=3001

wsSocketTimeout = 30 

# !! when giving the moduleID at the commandline, put the value in single quotes, otherways the bash
# will expand the $7.. to something wrong..
#     --variable 'moduleID:$740'
moduleID = "$741"


donglePort = "/tmp/DXM"

# how to get a slack mesage url, see https://api.slack.com/incoming-webhooks
# encode the URL by echo -n "https://hooks.slack.com/services/<yourpersonalChannelKey>" | base64
slackURL = "aHR0cHM6Ly9ob29rcy5zbGFjay5jb20vc2VydmljZXMvYmxhYmx1Yg=="