from urllib.parse import urlencode, urlparse
from urllib.request import Request, urlopen
import argparse
import os
import base64
import sys

'''
ATTENTION:This script uses the webUIClient from the oobd testing packages. To make it work, copy oobd/testing/robotframework/lib/webUIClient.py into this directory
'''
import webUIClient

parser = argparse.ArgumentParser( formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument("--url", help="the URL the OOBD HTTP daemon runs on", default = "http://localhost:8080")
parser.add_argument("--wsPort", help="the websocket port where the OOBD daemon listens on",  default='8443')
parser.add_argument("--wsTimeout", help="timeout for webSocket answers",   default=30)
#parser.add_argument("--settings", help="the file path to an optional settings json file")
parser.add_argument("--password", help="the password, which might protect the settings", default ="bob")
parser.add_argument("--initsteps", help="the expected number of incoming commands, before we can do our function call", default =20)
parser.add_argument("--lastinitcmd", help="the last incoming command, which indicates the end of the init process and triggers our own function call", default ='{"type":"PAGEDONE"}')
parser.add_argument("--function", help="the function name to call", default ="autorun")
parser.add_argument("--resultRegex", help="regex of the expected value result", default =".*")
parser.add_argument("-j", "--json", help="dumps the received json answer", action="store_true")
parser.add_argument("--optid", help="the optionalID of the function call", default ="")
parser.add_argument("--actValue", help="the actual value of the function call", default ="")
parser.add_argument("--updType", help="the update Type of the function call", default ="3")
parser.add_argument("script", help="the file path of the script to run")
args = parser.parse_args()

#first we evaluate the absolute path of the script
try:
	fullPath=os.path.dirname(os.path.abspath(args.script))
	fileName=os.path.basename(args.script)
	b64FileName=base64.b64encode(fileName.encode('utf8')).decode("utf-8") 

	# then we set the oobd script path to that directory
	post_fields = {"settings":'{"ScriptDir":"'+fullPath+'"}' , 'settingspw' : '"'+args.password+'"' } 
	request = Request(args.url+'/theme/default/settings.html', urlencode(post_fields).encode())
	json = urlopen(request).read().decode()

	#then we read the main page to trigger the scan of the actual new directory
	request = Request(args.url+'/')
	json = urlopen(request).read().decode()

	#now we load the script
	baseURL=args.url+'/'+b64FileName
	request = Request(args.url+'/'+b64FileName)
	json = urlopen(request).read().decode()

	#connect to the webSocket
	urlDetails = urlparse(args.url)
	ws=webUIClient.webUIClient()
	ws.quiet(True)
	ws.open_webUI( 'ws://'+urlDetails.hostname+':'+args.wsPort, args.wsTimeout)
	#jump over the first messages coming from OOBD

	errorCount= args.initsteps
	while errorCount>0:
		try:
			ans=ws.answer_should_match(args.lastinitcmd)
			errorCount=-1
		except AssertionError:
			errorCount-=1
	if errorCount>-1: # the end of the init sequence was not found, so break with an error code
		sys.exit(1)
	ws.send_webUI_command('{"name":"'+args.function+':","optid":"'+args.optid+'","actValue":"'+args.actValue+'","updType":'+args.updType+'}')
	try:
		ans=ws.answer_should_match('{"type":"VALUE" ,"value":"%'+args.resultRegex+'"}')
		
		if args.json:
			print(ws._answer)
			exit(0)
		else:
			res=int(ws.jsonAnswer["value"])
			sys.exit(res)
		
	except AssertionError:
		if args.json:
			print(ws._answer)
		sys.exit(2)



	ws.close_webUI()
except Exception as e:
	print("runtime error: "+repr(e))
	sys.exit(127)