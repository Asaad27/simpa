#!/bin/bash

function usage {
    echo -e "Usage : $0 [--limit X] config_file.json\n\t--limit X : limit the crawling phase to X seconds"
    echo
    simpaMissing
    exit
}

function simpaMissing {
    echo "IMPORTANT : In order to work, you must first generate a file called SIMPA.jar corresponding to the project."
    echo "SIMPA.jar and the lib/ folder (containing the dependencies) must be in your working directory to execute this script"
}


if [[ $# == 0 ]]
then
    usage
fi

if $(test -f SIMPA.jar)
then
    echo
else
    simpaMissing
fi

LIMIT=-1
while [[ $# > 0 ]]
do
    ARG="$1"
    case $ARG in
        --limit)
            if [ "$2" == "" ]
            then
                usage
            fi
            LIMIT="$2"
            shift
            ;;
        *.json)
            FILENAME=$ARG
            ;;
        *)
            usage
    esac
    shift
done

#crawling
if [ $LIMIT != "-1" ]
then
    java -cp SIMPA.jar main.drivergen.DriverGen --limit $LIMIT $FILENAME
else
    java -cp SIMPA.jar main.drivergen.DriverGen $FILENAME
fi


#inference+XSS detection
java -cp SIMPA.jar main.simpa.SIMPA --weka --xss --text --generic abs/$(basename $FILENAME .json).xml
