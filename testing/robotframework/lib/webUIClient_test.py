import webUIClient


from json import loads, dumps
from base64 import encodestring, decodestring


client = webUIClient.webUIClient()
client.open_webUI("ws://localhost:8443")
client.send_webUI_command(' { "name" : "test" , "to" : { "recp" : "Müller" } } ')
client.answer_should_match('{"type":"WRITESTRING" ,"data":"%#.*(OBD).*"}')
client.close_webUI()

#print ("Compare: ", client.compareDicts(
	#loads(' { "name" : "test" , "to" : { "recp" : "Müller" , "nr" : 5 } } ') ,
	#loads(' { "name" : "test" , "to" : { "recp" : "Müller" , "nr" : "5" } } ')
	#))