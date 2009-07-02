EESchema Schematic File Version 2  date 02.07.2009 20:28:51
LIBS:power,xilinx,valves,ttl_ieee,transistors,texas,SymbolsSimilarEN60617+oldDIN617,special,siliconi,regul,ramtron,pspice,philips,pca82c250,opto,oobd_d1,motorola,microcontrollers,microchip1,microchip,memory,lm339,linear,interface,intel,graphic,gennum,elec-unifil,dsp,display,digital-audio,cypress,contrib,conn,cmos_ieee,cmos4000,brooktre,audio,atmel,analog_switches,adc-dac,74xx,device,.\oobd_top.cache
EELAYER 24  0
EELAYER END
$Descr A4 11700 8267
Sheet 1 4
Title ""
Date "5 jun 2009"
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
Wire Wire Line
	3950 4550 4750 4550
Wire Wire Line
	3950 4250 4750 4250
Wire Wire Line
	3950 3950 4750 3950
Wire Wire Line
	3950 3650 4750 3650
Wire Wire Line
	3950 3350 4750 3350
Wire Wire Line
	3950 3050 4750 3050
Wire Wire Line
	3950 2750 4750 2750
Wire Wire Line
	3950 2450 4750 2450
Wire Wire Line
	7450 3500 8500 3500
Wire Wire Line
	7450 3200 8500 3200
Wire Wire Line
	7450 2900 8500 2900
Wire Wire Line
	7450 2600 8500 2600
Wire Wire Line
	7450 2450 8500 2450
Wire Wire Line
	8500 2750 7450 2750
Wire Wire Line
	8500 3050 7450 3050
Wire Wire Line
	8500 3350 7450 3350
Wire Wire Line
	8500 3650 7450 3650
Wire Wire Line
	7450 3800 8500 3800
Wire Wire Line
	4750 2600 3950 2600
Wire Wire Line
	4750 2900 3950 2900
Wire Wire Line
	4750 3200 3950 3200
Wire Wire Line
	4750 3500 3950 3500
Wire Wire Line
	4750 3800 3950 3800
Wire Wire Line
	4750 4100 3950 4100
Wire Wire Line
	4750 4400 3950 4400
Text Notes 1200 7550 0    60   ~ 0
All device Numbers (if nesssary) xx  / sheet names no capital letters
$Sheet
S 4750 2050 2700 4800
U 4A291F27
F0 "up_mainboard" 60
F1 "up_mainboard.sch" 60
F2 "12V" I L 4750 4550 60 
F3 "KL_IN" I L 4750 2450 60 
F4 "K_OUT" O L 4750 2600 60 
F5 "L_OUT" O L 4750 2750 60 
F6 "PWM+_OUT" O L 4750 2900 60 
F7 "PWM-_OUT" O L 4750 3050 60 
F8 "PWM_IN" I L 4750 3200 60 
F9 "VPWM_OUT" O L 4750 3350 60 
F10 "VPWM_IN" I L 4750 3500 60 
F11 "CAN_TXD" O L 4750 3650 60 
F12 "CAN_RXD" I L 4750 3800 60 
F13 "CANSEL_1" O L 4750 3950 60 
F14 "CANSEL_2" O L 4750 4100 60 
F15 "+5V" O L 4750 4250 60 
F16 "GND" U L 4750 4400 60 
F17 "TXD1" O R 7450 2450 60 
F18 "RXD1" I R 7450 2750 60 
F19 "+5V" O R 7450 2900 60 
F20 "GND" U R 7450 2600 60 
F21 "NC" U R 7450 3050 60 
F22 "NC" U R 7450 3200 60 
F23 "NC" U R 7450 3350 60 
F24 "RXD0" I R 7450 3500 60 
F25 "NC" U R 7450 3650 60 
F26 "TXD0" O R 7450 3800 60 
$EndSheet
$Sheet
S 8500 2050 1750 4600
U 4A291AD5
F0 "pc_interface" 60
F1 "pc_interface.sch" 60
F2 "TXD1" I L 8500 2450 60 
F3 "GND" U L 8500 2600 60 
F4 "RXD1" O L 8500 2750 60 
F5 "+5V" I L 8500 2900 60 
F6 "NC" U L 8500 3050 60 
F7 "NC" U L 8500 3200 60 
F8 "NC" U L 8500 3350 60 
F9 "RXD0" O L 8500 3500 60 
F10 "NC" U L 8500 3650 60 
F11 "TXD0" I L 8500 3800 60 
$EndSheet
$Sheet
S 1350 2050 2600 4800
U 4A291A4E
F0 "obd_interface" 60
F1 "obd_Interface.sch" 60
F2 "+5V" I R 3950 4250 60 
F3 "GND" U R 3950 4400 60 
F4 "KL_IN" O R 3950 2450 60 
F5 "K_OUT" I R 3950 2600 60 
F6 "L_OU" I R 3950 2750 60 
F7 "12V" O R 3950 4550 60 
F8 "PWM+_OUT" I R 3950 2900 60 
F9 "PWM-_OUT" I R 3950 3050 60 
F10 "PWM_IN" O R 3950 3200 60 
F11 "VPWM_OUT" I R 3950 3350 60 
F12 "VPWM_IN" O R 3950 3500 60 
F13 "CAN_TXD" I R 3950 3650 60 
F14 "CAN_RXD" O R 3950 3800 60 
F15 "CANSEL_1" I R 3950 3950 60 
F16 "CANSEL_2" I R 3950 4100 60 
$EndSheet
$EndSCHEMATC
