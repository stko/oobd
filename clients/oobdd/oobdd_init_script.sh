#! /bin/sh
#  /etc/init.d/oobdd

### BEGIN INIT INFO
# Provides:          oobdd
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Short-Description: Starts the oobdd service
# Description:       This file is used to start the daemon
#                    and should be placed in /etc/init.d
### END INIT INFO

# Author:   Steffen Koehler <steffen[AT]koehlers.de>
# URL:      www.oobd.org
# Date:     25/09/2016

# derivated from: http://www.neilson.co.za/creating-a-java-daemon-system-service-for-debian-using-apache-commons-jsvc/

NAME="oobdd"
DESC="oobdd service"

# The path to Jsvc
EXEC="/usr/bin/jsvc"

# The path to the folder containing oobdd.jar
FILE_PATH="/home/oobd/oobdd/"

# The path to the folder containing the java runtime
JAVA_HOME="/usr/lib/jvm/default-java"

# Our classpath including our jar file and the Apache Commons Daemon library
#CLASS_PATH="$FILE_PATH/oobdd.jar:$FILE_PATH/lib/commons-daemon-1.0.15.jar"
CLASS_PATH="$FILE_PATH/oobdd.jar:$FILE_PATH/lib/"

# The fully qualified name of the class to execute
CLASS="oobdd.Oobdd"

# Any command line arguments to be passed to the our Java Daemon implementations init() method 
ARGS="--settings=/home/oobd/oobdd/settings.json"

#The user to run the daemon as
USER="oobd"

# The file that will contain our process identification number (pid) for other scripts/programs that need to access it.
PID="/var/run/$NAME.pid"

# System.out writes to this file...
LOG_OUT="$FILE_PATH/log/$NAME.out"

# System.err writes to this file...
LOG_ERR="$FILE_PATH/err/$NAME.err"

jsvc_exec()
{   
    cd $FILE_PATH
    $EXEC -home $JAVA_HOME -cp $CLASS_PATH -user $USER -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS $ARGS
}

case "$1" in
    start)  
        echo "Starting the $DESC..."        
        
        # Start the service
        jsvc_exec
        
        echo "The $DESC has started."
    ;;
    stop)
        echo "Stopping the $DESC..."
        
        # Stop the service
        jsvc_exec "-stop"       
        
        echo "The $DESC has stopped."
    ;;
    restart)
        if [ -f "$PID" ]; then
            
            echo "Restarting the $DESC..."
            
            # Stop the service
            jsvc_exec "-stop"
            
            # Start the service
            jsvc_exec
            
            echo "The $DESC has restarted."
        else
            echo "Daemon not running, no action taken"
            exit 1
        fi
            ;;
    *)
    echo "Usage: /etc/init.d/$NAME {start|stop|restart}" >&2
    exit 3
    ;;
esac
