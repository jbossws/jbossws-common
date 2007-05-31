#!/bin/sh

# $Id: wsgen.sh 2158 2007-01-27 06:20:59Z jason.greene@jboss.com $

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

#JPDA options. Uncomment and modify as appropriate to enable remote debugging .
#JAVA_OPTS="-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y $JAVA_OPTS"

# Setup JBoss sepecific properties
JAVA_OPTS="$JAVA_OPTS"

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

# is it a JBossWS-native or SunRI installation?
if [ -a $JBOSS_HOME/client/jbossws-client.jar ]; then
    JBOSSWS_NATIVE="true"
fi

#
# Setup the wsconsume classpath
# The classpath is dynamically build depending on the stack that
# is deployed. See $JBOSSWS_NATIVE above.
#

# shared libs
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JAVA_HOME/lib/tools.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/activation.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/getopt.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/wstx.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossall-client.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/log4j.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/mail.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-spi.jar"

# shared jaxws libs 
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxws-tools.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxws-rt.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxb-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxb-impl.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxb-xjc.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/stax-api.jar"

# stack specific dependencies
if [ "x$JBOSSWS_NATIVE" = "x" ]; then
   echo "Seems to be a Sun-RI stack deployed"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-sunri-client.jar"
else
   echo "Seems to be a JBossWS-Native stack deployed"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/javassist.jar"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jboss-xml-binding.jar"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-client.jar"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jboss-jaxws.jar"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jboss-jaxrpc.jar"
   WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jboss-saaj.jar"
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    WSCONSUME_CLASSPATH=`cygpath --path --windows "$WSCONSUME_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

# Execute the JVM
"$JAVA" $JAVA_OPTS \
   -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
   -Dlog4j.configuration=wstools-log4j.xml \
   -classpath "$WSCONSUME_CLASSPATH" \
   org.jboss.wsf.spi.tools.cmd.WSConsume "$@"
