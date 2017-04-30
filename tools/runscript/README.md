# runscript - batch controlled script execution

When having OOBD running like a webservice, it always need some user interaction first to load a script.

But in some cases its prefered that a script runs by it's own, like on a self running dongle start-up, on external events etc.

For such purposes runscript was made. This little python script, derived from the automated testing, controls oobd from outside and let's it load and run a script.


When setting up runscript, keep in mind that the parameters of the function call and the returned results must be aligned between the runscript and the lua script. A pure generic use would create quite random results ;-)



runscript supports several options. The majority of them are not needed for the standard case, but they allow some fine adjustments, when needed
```
usage: runscript.py [-h] [--url URL] [--wsPort WSPORT] [--wsTimeout WSTIMEOUT]
                    [--password PASSWORD] [--initsteps INITSTEPS]
                    [--lastinitcmd LASTINITCMD] [--function FUNCTION]
                    [--resultRegex RESULTREGEX] [-j] [--optid OPTID]
                    [--actValue ACTVALUE] [--updType UPDTYPE]
                    script

positional arguments:
  script                the file path of the script to run

optional arguments:
  -h, --help            show this help message and exit
  --url URL             the URL the OOBD HTTP daemon runs on (default:
                        http://localhost:8080)
  --wsPort WSPORT       the websocket port where the OOBD daemon listens on
                        (default: 8443)
  --wsTimeout WSTIMEOUT
                        timeout for webSocket answers (default: 30)
  --password PASSWORD   the password, which might protect the settings
                        (default: bob)
  --initsteps INITSTEPS
                        the expected number of incoming commands, before we
                        can do our function call (default: 20)
  --lastinitcmd LASTINITCMD
                        the last incoming command, which indicates the end of
                        the init process and triggers our own function call
                        (default: {"type":"PAGEDONE"})
  --function FUNCTION   the function name to call (default: autorun)
  --resultRegex RESULTREGEX
                        regex of the expected value result (default: .*)
  -j, --json            dumps the received json answer (default: False)
  --optid OPTID         the optionalID of the function call (default: )
  --actValue ACTVALUE   the actual value of the function call (default: )
  --updType UPDTYPE     the update Type of the function call (default: 3)
```

## The Options

The most options are already explained above, so here are the more special ones:

### wsTimeout
After calling the function, runscript waits for a response for wsTimeout seconds. This value might be increased for long time running calls.



### password
the oobd settings might be protected against user modifications by a password, but to load a script from a different place, as runscript does, we need to give the password to change the default script directory

### initstep & lastinitcommand
Before we can call a function in a oobd script, the scripts runs through different initialisation steps, which are signaled through messages. Two of them are standard (WSCONNECT and WRITESTRING), but there might be more depending on if the script will try e.g. to build a user menu.

with initstep you can define a maximum how many of these upfront messages will be ignored while waiting of the message defined by lastinitcommand, which tells runscript, that the initialisation process has now reached its end and the wanted function can be called now.

### function, , optid, actValue, updType
Function is (obviously) the function which is called, and as usual in oobd lua scripts, these functions will be feed with optid, actValue and updType, so if needed in special cases, some different values can be given.

### resultregex & json
A oobd function call always returns a json object, which contains beside others the result value, encoded as base64 string.
runscript identifies a function result as valid, if the returned json object contains a property called value, and the base64 encoded string of that value fits to the regular expression string given by `resultregex`.


if `json` is not set, then runscript assumes that the returned value is a numeric value, which is then used to generate the runscript program exit code. By that the lua script can define the program exit code of runscript

if `json` is set, then runscript acts different: First, if the regex matches, then the program exit code is 0, otherways 2. But for futher processing, e.g. with jq, the complete oobd answer json object is dumped to stdout.




## Notice
The script manipulates the script directory settings to the directory where the script is in


## Examples

The basic one is simple: All defaults matches, and the script function named autorun returns a numeric value of 0, so the program exists also with 0

	python3 runscript.py testscript.lus

And now a full flavoured one:  
Its connects to another machine where the oobd daemon runs on, loads the script testsuite.lbc, starts the function interface_version, which returns in this case the dongle ID string. To make this visible, the whole oobd json answer is dumped to stdout by the flag --json. There's its futher processed by the json filter tool `jq`, which filters the result value out of the stream. As the result values are base64 encoded, the `base64`tool then finally decrypts this back to its original format.

	python3 runscript.py --url=http://steffen-Desktop64.fritz.box:8080 /media/ram/testsuite.lbc --function "interface_version" -j | jq -j ".value" | base64 -d
	
	OOBD POSIX V2.0alpha1-46-ge74bd35-dirty POSIX Sat, 18 Mar 2017 12:46:29 +0100
