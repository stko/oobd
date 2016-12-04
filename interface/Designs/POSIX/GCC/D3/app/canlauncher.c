/*
	
	This file is part of the OOBD.org distribution.

	OOBD.org is free software; you can redistribute it and/or modify it
	under the terms of the GNU General Public License (version 2) as published
	by the Free Software Foundation and modified by the FreeRTOS exception.

	OOBD.org is distributed in the hope that it will be useful, but WITHOUT
	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
	more details.

	You should have received a copy of the GNU General Public License along
	with FreeRTOS.org; if not, write to the Free Software Foundation, Inc., 59
	Temple Place, Suite 330, Boston, MA  02111-1307  USA.


	OOBD C source files requirement:
	Unix EOL file format 
	UTF-8
	formated with "indent -kr"
	  
	Please ensure to read the configuration and relevant port sections of the
	online documentation.


*/



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <spawn.h>
#include <sys/wait.h>
#include <time.h>

// apt-get install libsocketcan2
#include <libsocketcan.h>

// posix specific argument handling
#include <getopt.h>

/* Flag set by ‘--verbose’. */
static int verbose_flag = 1;

// loop counters
static int sleeps = 100, retries = 0, retriesCnt, loops = 0, loopsCnt;

//part of the posix_spawn environment
extern char **environ;

//defines, if the conditions for another loop are met
int doAnotherLoop()
{
    return (retries == 0 || retries > 1) && (loopsCnt == 0
					     || loopsCnt > 1);
}

//wait, that the can device becomes available. Returns false, if the wait times out
int waitforCanDevice(char *canDevice)
{
    retriesCnt = retries;
    if (verbose_flag)
	printf("wait for %s can device\n", canDevice);
    do {
	if (retriesCnt > 1)
	    retriesCnt--;
	int canState;
	int canStateCall = can_get_state(canDevice, &canState);
	if (verbose_flag)
	    printf("can_get_state returns %d with a device state of %d\n",
		   canStateCall, canState);
	if ((canStateCall == 0) && (canState != CAN_STATE_STOPPED))
	    return 1;
	if (nanosleep((const struct timespec[]) { {
		      sleeps / 1000, (sleeps % 1000) * 1000000L}},
		      NULL) == -1) {
	    perror("nanosleep reports");
	}

    } while (!(retriesCnt == 1));
    return 0;
}

void print_usage()
{
    printf
	("\ncanlaucher - (re)starts program on can device availability\n");
    printf
	("Canlaucher is part of the OOBD Open OnBoard Diagnostic Toolset (www.oobd.org) \n\n");
    printf
	("canlauncher [--verbose] [--loops nrOfLoops] [--sleep sleepTime]\n");
    printf
	("                   [--retries retries] canDevice [program [args..]]\n\n");
    printf("--loops (-l) nrOfLoops:\n");
    printf
	("                   Nr of successful 'Can Device is available'\n");
    printf("                   detects before program termination.\n");
    printf("                   Default is 0 = runs forever\n");
    printf("--sleep (-s) sleepTime:\n");
    printf
	("                   Waiting time for can device in ms per cycle.\n");
    printf("                   Default 100ms, minimum 10ms\n");
    printf("--retries (-r) retries:\n");
    printf("                   Nr of retries during one waiting cycle.\n");
    printf
	("                   Total waiting time per cycle is retries * sleeptime.\n");
    printf("                   Default is 0 = retry endless\n");
    printf("canDevice:\n");
    printf("                   SocketCan Device to wait for\n");
    printf("program [args..] :\n");
    printf
	("                   program to start. If not given, Canlaucher just runs\n");
    printf
	("                   through the given loop/retry sequence and terminates\n");
}


int main(int argc, char *argv[])
{
    int c;


    static struct option long_options[] = {
	/* These options set a flag. */
	{"verbose", no_argument, &verbose_flag, 1},
	{"brief", no_argument, &verbose_flag, 0},
	/* These options don’t set a flag.
	   We distinguish them by their indices. */
	{"sleep", required_argument, 0, 's'},
	{"loops", required_argument, 0, 'l'},
	{"retries", required_argument, 0, 'r'},
	{0, 0, 0, 0}
    };
    /* getopt_long stores the option index here. */
    int option_index = 0;
    while (1) {
	c = getopt_long(argc, argv, "s:l:r:", long_options, &option_index);

	/* Detect the end of the options. */
	if (c == -1)
	    break;

	switch (c) {
	case 0:
	    /* If this option set a flag, do nothing else now. */
	    if (long_options[option_index].flag != 0)
		break;
	    if (verbose_flag)
		printf("option %s", long_options[option_index].name);
	    if (optarg)
		if (verbose_flag)
		    printf(" with arg %s", optarg);
	    if (verbose_flag)
		printf("\n");
	    break;


	case 's':
	    sleeps = atoi(optarg);
	    if (sleeps < 10)
		sleeps = 10;
	    if (verbose_flag)
		printf("option -s: Set sleep between trials to `%s ms'\n",
		       optarg);
	    break;

	case 'r':
	    retries = atoi(optarg);
	    if (retries < 0)
		retries = 0;
	    if (verbose_flag)
		printf("option -r: Retry counter to `%s'\n", optarg);
	    if (retries > 0)
		retries++;	//add 1 for the while mechanism
	    break;


	case 'l':
	    loops = atoi(optarg);
	    if (loops < 0)
		loops = 0;
	    if (verbose_flag)
		printf("option -l: Set loop counter to `%s'\n", optarg);
	    if (loops > 0)
		loops++;	//add 1 for the while mechanism
	    break;


	case '?':
	    /* getopt_long already printed an error message. */
	    break;

	default:
	    print_usage();
	    abort();
	}
    }

    /* Instead of reporting ‘--verbose’
       and ‘--brief’ as they are encountered,
       we report the final status resulting from them. */
    if (verbose_flag)
	if (verbose_flag)
	    puts("verbose flag is set");

    /* Print any remaining command line arguments (not options). */
    if (optind < argc) {
	int commandArgIndex = optind;
	if (verbose_flag)
	    printf("non-optional command line arguments: ");
	if (verbose_flag) {
	    while (optind < argc) {
		printf("%s ", argv[optind++]);
	    }
	    putchar('\n');
	}

	pid_t pid;
	int status;
	loopsCnt = loops;
	while (doAnotherLoop()) {
	    if (waitforCanDevice(argv[commandArgIndex])) {	// wait for given CAN Device
		if (commandArgIndex < argc - 1) {	// do the command line contains also a executable?
		    if (verbose_flag)
			printf("Run command: %s\n",
			       argv[commandArgIndex + 1]);
		    // argv - array is null terminated: http://stackoverflow.com/a/11020198
		    // see also http://docs.oracle.com/cd/E19048-01/chorus5/806-6894/6jfqa5bt1/index.html for posix_spawnp
		    status =
			posix_spawnp(&pid, argv[commandArgIndex + 1], NULL,
				     NULL, &argv[commandArgIndex + 1],
				     environ);
		    if (status == 0) {
			if (verbose_flag)
			    printf("Child pid: %i\n", pid);
			if (waitpid(pid, &status, 0) != -1) {
			    if (verbose_flag)
				printf("Child exited with status %i\n",
				       status);
			} else {
			    if (verbose_flag)
				perror("waitpid");
			}
		    } else {
			if (verbose_flag)
			    printf("posix_spawn: %s\n", strerror(status));
			loopsCnt = 1;
			retriesCnt = 1;
		    }
		}
	    }
	    if (loopsCnt > 1)
		loopsCnt--;
	}
    } else {
	printf("Error: missing CAN Device parameter\n");
	print_usage();
    }

}
