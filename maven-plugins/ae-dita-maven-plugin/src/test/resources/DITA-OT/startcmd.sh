#!/bin/sh
#  This file is part of the DITA Open Toolkit project hosted on 
#  Sourceforge.net. See the accompanying license.txt file for 
#  applicable licenses.
#  (c) Copyright IBM Corp. 2006 All Rights Reserved.

if  [[ "$DITA_HOME" = "" ]]; then 
   echo "DITA_HOME environment variable not set";
   exit 127;
fi

cd "$DITA_HOME"

# Get the absolute path of DITAOT's home directory
DITA_DIR="`pwd`"

if [ ! -x "$DITA_DIR"/tools/ant/bin/ant ]; then
chmod +x "$DITA_DIR"/tools/ant/bin/ant
fi

export ANT_OPTS="-Xmx512m $ANT_OPTS"
export ANT_HOME="$DITA_DIR"/tools/ant
export PATH="$DITA_DIR"/tools/ant/bin:"$PATH"

NEW_CLASSPATH="$DITA_DIR/lib:$DITA_DIR/lib/dost.jar:$DITA_DIR/lib/resolver.jar:$DITA_DIR/lib/fop.jar:$DITA_DIR/lib/avalon-framework-cvs-20020806.jar:$DITA_DIR/lib/batik.jar:$DITA_DIR/lib/saxon.jar:$DITA_DIR/lib/saxon65.jar:$DITA_DIR/lib/xml-apis.jar:$DITA_DIR/lib/icu4j.jar:$DITA_DIR/lib/xslthl-2.0.0.jar:$DITA_DIR/lib/fonts.jar:$DITA_DIR/lib/fop-hyph.jar"
if test -n "$CLASSPATH"
then
export CLASSPATH="$NEW_CLASSPATH":"$CLASSPATH"
else
export CLASSPATH="$NEW_CLASSPATH"
fi

sh
