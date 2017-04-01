
/*
	The main page contains default settings for very few  modules and their IDs and bus settings.
	As these information from the different car manufactures are mostly not public, that array there is quite empty.. - but
	in this package you'll also find the file README_vehicle.js.txt
	When you rename this file to vehicle.js and place your personal vehicle settings into it, then that file will be
	loaded at start and its content will override the default settings 
*/


/*
 * Module IDs found on https://docs.google.com/spreadsheets/d/1yax6zfhZYj2joBczEeruqKh9X5Qhee3C0ngilqwTA7E/pubhtml?gid=0&single=true
 */

// Reminder: in the OOBD data exchange the numeric module adresses are represented as string and so also used as hash key

var carmakers = [
	{
		name:     "Ford",
		modules : {
		"701" : "GPSM - Global Positioning System Module",
		"714" : "HSWM - Heated Steering Wheel Module",
		"720" : "IPC - Instrument Panel Cluster",
		"726" : "BCM - Body Control Module",
		"727" : "ACM - Audio Control Module",
		"730" : "PSCM - Power Steering Control Module",
		"731" : "KVM - Keyless Vehicle Module",
		"733" : "HVAC - Heating Ventilation Air Condition",
		"737" : "RCM - Restraints Control Module",
		"740" : "DDM - Driver Front Door Module",
		"741" : "PDM - Passenger Door Module",
		"760" : "ABS - Anti-Lock Brake System (ABS) Control Module",
		"793" : "FDSM - Front Distance Sensing Module",
		"7A5" : "FCDIM - Multi Function Display",
		"7A7" : "FCIM - Front Controls Interface Module",
		"7D0" : "APIM - Accessory Protocol Interface Module",
		"7E0" : "PCM - Powertrain Control Module",
		"7E1" : "TCM - Transmission Control Module" ,
		}
	} 
];