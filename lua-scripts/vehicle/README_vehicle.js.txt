
/*
	The main page contains default settings for very few  modules and their IDs and bus settings.
	As these information from the different car manufactures are mostly not public, that array there is quite empty.. - but
	in this package you'll also find the file README_vehicle.js.txt
	When you rename this file to vehicle.js and place your personal vehicle settings into it, then that file will be
	loaded at start and its content will override the default settings 
*/




var carlines = [
	{
		name:     "my car 1",
		modules:    [
			{
				name:  "Module A",
				id: "125b11_726"
			},
			{
				name:  "Module B",
				id: "500b11_7E0"
			}
		]
	} ,
	{
		name:     "my car 2",
		modules:    [
			{
				name:  "Module A",
				id: "125b11_726"
			},
			{
				name:  "Module B",
				id: "500b11_7E0"
			}
		]
	} ,
];