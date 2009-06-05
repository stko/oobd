EESchema Schematic File Version 2  date 05.06.2009 16:29:45
LIBS:power,xilinx,valves,ttl_ieee,transistors,texas,SymbolsSimilarEN60617+oldDIN617,special,siliconi,regul,ramtron,pspice,philips,pca82c250,opto,oobd_d1,motorola,microcontrollers,microchip1,microchip,memory,lm339,linear,interface,intel,graphic,gennum,elec-unifil,dsp,display,digital-audio,cypress,contrib,conn,cmos_ieee,cmos4000,brooktre,audio,atmel,analog_switches,adc-dac,74xx,device,.\OOBD_Top.cache
EELAYER 24  0
EELAYER END
$Descr A4 11700 8267
Sheet 2 4
Title "Mainboard"
Date "30 may 2009"
Rev "1"
Comp "www.oobd.org"
Comment1 "D1"
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
Text Notes 850  7550 0    60   ~ 15
All device numbers 1xx
Wire Wire Line
	4050 4100 4050 5150
Wire Wire Line
	6600 4250 6600 1900
Wire Wire Line
	7000 4450 7000 1500
Wire Wire Line
	6800 4650 6800 1700
Wire Wire Line
	6500 2000 6500 4750
Wire Wire Line
	5400 4750 5400 4900
Wire Wire Line
	6150 1400 7100 1400
Wire Wire Line
	8900 4350 7100 4350
Wire Wire Line
	6150 1600 6900 1600
Wire Wire Line
	8900 4550 6900 4550
Wire Wire Line
	6150 1800 6700 1800
Wire Wire Line
	8900 4850 6700 4850
Wire Wire Line
	6500 2000 6150 2000
Wire Wire Line
	6500 4750 8900 4750
Wire Wire Line
	4050 4100 4350 4100
Wire Wire Line
	4050 5150 8900 5150
Wire Wire Line
	4350 4200 4150 4200
Wire Wire Line
	8750 5600 8750 5550
Wire Wire Line
	8750 5550 8900 5550
Wire Wire Line
	1600 1600 1600 1450
Wire Wire Line
	750  1450 750  1650
Wire Wire Line
	750  850  750  1050
Wire Wire Line
	1750 3800 2000 3800
Wire Wire Line
	2000 3800 2000 3700
Wire Wire Line
	2000 3700 4350 3700
Wire Wire Line
	1750 3400 2150 3400
Wire Wire Line
	2150 3400 2150 3500
Connection ~ 700  3600
Wire Wire Line
	850  3600 700  3600
Wire Wire Line
	850  3200 700  3200
Wire Wire Line
	700  3200 700  4050
Wire Wire Line
	1900 2900 1900 3200
Wire Wire Line
	1900 3200 1750 3200
Wire Wire Line
	700  3400 850  3400
Connection ~ 700  3400
Wire Wire Line
	850  3800 700  3800
Connection ~ 700  3800
Wire Wire Line
	1750 3600 4350 3600
Wire Wire Line
	1600 1050 1600 850 
Wire Wire Line
	800  1000 750  1000
Connection ~ 750  1000
Wire Wire Line
	1200 1400 1200 1300
Wire Wire Line
	8900 5450 8750 5450
Wire Wire Line
	8900 5650 8600 5650
Wire Wire Line
	8900 5050 4150 5050
Wire Wire Line
	6150 2100 6400 2100
Wire Wire Line
	6600 4250 8900 4250
Wire Wire Line
	6600 1900 6150 1900
Wire Wire Line
	6800 4650 8900 4650
Wire Wire Line
	6800 1700 6150 1700
Wire Wire Line
	7000 4450 8900 4450
Wire Wire Line
	7000 1500 6150 1500
Wire Wire Line
	5400 750  5400 650 
Wire Wire Line
	8900 4950 6400 4950
Wire Wire Line
	6400 4950 6400 2100
Wire Wire Line
	6700 4850 6700 1800
Wire Wire Line
	6900 4550 6900 1600
Wire Wire Line
	7100 4350 7100 1400
Wire Wire Line
	4150 5050 4150 4200
$Comp
L +12V #PWR?
U 1 1 4A2215E6
P 5400 650
F 0 "#PWR?" H 5400 600 20  0001 C CNN
F 1 "+12V" H 5400 750 30  0000 C CNN
	1    5400 650 
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A2215DF
P 5400 4900
F 0 "#PWR?" H 5400 4900 30  0001 C CNN
F 1 "GND" H 5400 4830 30  0001 C CNN
	1    5400 4900
	1    0    0    -1  
$EndComp
$Comp
L +12V #PWR?
U 1 1 4A221417
P 8600 5650
F 0 "#PWR?" H 8600 5600 20  0001 C CNN
F 1 "+12V" H 8600 5750 30  0000 C CNN
	1    8600 5650
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A22140C
P 8750 5600
F 0 "#PWR?" H 8750 5600 30  0001 C CNN
F 1 "GND" H 8750 5530 30  0001 C CNN
	1    8750 5600
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR?
U 1 1 4A2213DA
P 8750 5450
F 0 "#PWR?" H 8750 5540 20  0001 C CNN
F 1 "+5V" H 8750 5540 30  0000 C CNN
	1    8750 5450
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR?
U 1 1 4A221368
P 1600 850
F 0 "#PWR?" H 1600 940 20  0001 C CNN
F 1 "+5V" H 1600 940 30  0000 C CNN
	1    1600 850 
	1    0    0    -1  
$EndComp
$Comp
L +12V #PWR?
U 1 1 4A221334
P 750 850
F 0 "#PWR?" H 750 800 20  0001 C CNN
F 1 "+12V" H 750 950 30  0000 C CNN
	1    750  850 
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A221327
P 750 1650
F 0 "#PWR?" H 750 1650 30  0001 C CNN
F 1 "GND" H 750 1580 30  0001 C CNN
	1    750  1650
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A221322
P 1200 1400
F 0 "#PWR?" H 1200 1400 30  0001 C CNN
F 1 "GND" H 1200 1330 30  0001 C CNN
	1    1200 1400
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A22131A
P 1600 1600
F 0 "#PWR?" H 1600 1600 30  0001 C CNN
F 1 "GND" H 1600 1530 30  0001 C CNN
	1    1600 1600
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A21785B
P 2150 3500
F 0 "#PWR?" H 2150 3500 30  0001 C CNN
F 1 "GND" H 2150 3430 30  0001 C CNN
	1    2150 3500
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR?
U 1 1 4A216FBD
P 1900 2900
F 0 "#PWR?" H 1900 2990 20  0001 C CNN
F 1 "+5V" H 1900 2990 30  0000 C CNN
	1    1900 2900
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR?
U 1 1 4A216F7D
P 700 4050
F 0 "#PWR?" H 700 4050 30  0001 C CNN
F 1 "GND" H 700 3980 30  0001 C CNN
	1    700  4050
	1    0    0    -1  
$EndComp
$Comp
L FM24C64 IC131
U 1 1 4A216312
P 1350 3500
F 0 "IC131" H 950 3900 50  0000 L BNN
F 1 "FM24C64" H 950 3000 50  0000 L BNN
F 2 "ramtron-EIAJ-SOIC-8" H 1350 3650 50  0001 C CNN
	1    1350 3500
	1    0    0    -1  
$EndComp
$Comp
L OBD_I K101
U 1 1 4A2162E2
P 9000 4100
F 0 "K101" H 9250 4050 60  0000 C CNN
F 1 "OBD_I" H 9350 1550 60  0000 C CNN
	1    9000 4100
	1    0    0    -1  
$EndComp
$Comp
L OLAT90CAN IC110
U 1 1 4A2162CD
P 4800 1100
F 0 "IC110" H 4800 1100 60  0000 C CNN
F 1 "OLAT90CAN" H 5050 -2200 60  0000 C CNN
	1    4800 1100
	1    0    0    -1  
$EndComp
$Comp
L C C102
U 1 1 4A21553F
P 1600 1250
F 0 "C102" H 1650 1350 50  0000 L CNN
F 1 "C" H 1650 1150 50  0000 L CNN
	1    1600 1250
	1    0    0    -1  
$EndComp
$Comp
L C C101
U 1 1 4A215536
P 750 1250
F 0 "C101" H 800 1350 50  0000 L CNN
F 1 "C" H 800 1150 50  0000 L CNN
	1    750  1250
	1    0    0    -1  
$EndComp
$Comp
L 7805 U101
U 1 1 4A2154E5
P 1200 1050
F 0 "U101" H 1350 854 60  0000 C CNN
F 1 "7805" H 1200 1250 60  0000 C CNN
	1    1200 1050
	1    0    0    -1  
$EndComp
$EndSCHEMATC
