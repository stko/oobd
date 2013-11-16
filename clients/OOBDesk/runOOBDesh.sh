#!/bin/sh
java -jar -Djava.library.path=../skds/base_src/org/oobd/base/port/ -Djava.util.logging.config.file=logging.props dist/OOBDesk.jar
