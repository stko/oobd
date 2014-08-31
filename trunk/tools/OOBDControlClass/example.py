from oobdControl import *
import sys
import logging

# If True, all Messages will be shown at stderr. 
DEBUG = False


if any("-debug" in s for s in sys.argv):
	DEBUG = True

# Setup logging
if DEBUG:
	logging.basicConfig(stream=sys.stderr, level=logging.DEBUG)
else:
	logging.basicConfig(filename="debug.log", level=logging.DEBUG)

logging.debug('OOBD-Control DEBUG MODE')


# Setup a connection to the dongle and send & receive some stuff.
# Dont forget to edit the oobd_control.ini to some plausible data. 
# Mind that the CAN-Data and the CAN-ReqIDs are dummy values

OOBDControlInstance = OOBDControl() # Creates a new Instance of OOBDControl

OOBDControlInstance.connect() # connects to the dongle specified in the INI
OOBDControlInstance.configureCAN(ms_hs="hs", speed=["11b", 500], reqId="000") # configures CAN-Bus to highspeed at 500kbit/s, 11bit adressing on reqID 000
OOBDControlInstance.sendCanData(["FFFFFF"], checkAnswer=False) # sends some CAN data
OOBDControlInstance.TesterPresent(active=True, reqId="000") # starts sending tester present on reqID 000
OOBDControlInstance.sendCanData(["FFFFFF"], checkAnswer=False) # sends some CAN data
OOBDControlInstance.TesterPresent(active=False, reqId="000") # stops sending tester present on reqID 000
OOBDControlInstance.disconnect() # disconnects the dongle
