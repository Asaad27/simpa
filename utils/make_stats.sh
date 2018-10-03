#!/bin/bash

# DISCLAIMER : This script might help to produce statistical analysis
# of inference but it wasn't written in order to be easily usable. It
# is given in the hope it will help but for long-term, a better tool
# should be designed.

# This script is used to generate inference for statistical analysis.
# To use the script, you must fill the $wantedConfig array with needed
# configurations and then launch this script in a loop until all stats are
# inferred.

simpa_dir="$(dirname "$0")/../"
csv_dir="$simpa_dir/hit/stats/CSV/"
skipConfigFile="/tmp/skipConfigs"

#maximum number of inference for each configuration in one execution of the script
testPerLoop=20
# wanted number of inference for each configuration
limit=150

#create graphs at the end of the script
makeGraph=true

# array to be filled with wanted configurations
wantedconfig=()
# each configuration is a string describing the learner, the driver and options for the learner.
# Those configuration are described in function 'getNb' which count the number
# of occurence of one config in a CSV file, and also at the end of the script
# when the configuration must be changed into simpa's options.
#
# Here are the syntax for each learner :
# "hW $nbStates $driver $shortestCE $inputsNb $tryTrace $hInW $thirdInconsistencies $hzxw $knownW $adaptiveH $adaptiveW"
# "(RS|LocW|ZQ|Lm) $nbStates $driver $inputsNb"
#
# $nbStates is the number of states for random drivers. put '_' for non-generated drivers
# $inputsNb is the number of inputs for random drivers. put '_' for non-generated drivers
# $driver is the driver to use. It can be :
#       'ConnexRandom(randomOutputs)'
#       'ConnexRandom(oneOutputDiff)'
#       'Combined(Counter(3);ConnexRandom(randomOutputs))'
#       'dot_file(TCP_Windows8_Server__connected)' were TCP_Windows8_Server__connected is the name of one automata in Radboud benchmark
# $shortestCE indicate to use MrBean or perfect oracle
# $tryTrace indicate how to use trace. can be 'none' or 'simple' (others values are deprecated)
# $hInW activate the heuristic add h in W (incompatible with adaptive sequences)
# $thirdInconsistencies activate the search of third inconsistencies (must be enable when tryTrace is not 'none')
# $hzxw enable the use of dictionary
# $knownW launch hW with an already computed W-set (need a transparent driver)
# $adaptiveH indicate to use adaptive homing sequence
# $adaptiveW indicate to use an adaptive characterization



useAdaptiveH=false
useAdaptiveW=false

#can be use to make stats in two time, but you can just ignore this
skipConfigs=$([ -e "$skipConfigFile" ] && echo true || echo false)
skipConfigs="true"

scriptTime="$(stat -c %Y $0)"

#those without W-set
while read driver
do
        wantedconfig+=("RS _ $driver _ true")
        #$skipConfigs || wantedconfig+=("ZQ _ $driver _")
        wantedconfig+=("Lm _ $driver _")
        #wantedconfig+=("hW _ $driver false _ simple false true true false $useAdaptiveH $useAdaptiveW")
        wantedconfig+=("hW _ $driver false _ simple false true true false false false")
        wantedconfig+=("hW _ $driver false _ simple false true true false true true")
done << EOF
dot_file(TCP_Windows8_Server__connected)
EOF

#wantedconfig=()

while read driver
do
        $skipConfigs || wantedconfig+=("ZQ _ $driver _")
        wantedconfig+=("Lm _ $driver _")
        wantedconfig+=("RS _ $driver _ true")
        wantedconfig+=("LocW _ $driver _")
        wantedconfig+=("hW _ $driver false _ simple false true true false $useAdaptiveH $useAdaptiveW")
        wantedconfig+=("hW _ $driver false _ simple false true true true true false")
        wantedconfig+=("hW _ $driver false _ simple false true true false true true")
        #wantedconfig+=("hW _ $driver false _ none false false false false $useAdaptiveH $useAdaptiveW")
        #wantedconfig+=("hW _ $driver false _ none false false false false $useAdaptiveH true")
        #wantedconfig+=("hW _ $driver false _ simple false true true true $useAdaptiveH $useAdaptiveW")
done << EOF
dot_file(mqtt_unknown1)
dot_file(mqtt_ActiveMQ__invalid)
dot_file(mqtt_ActiveMQ__non_clean)
dot_file(mqtt_mosquitto__two_client_will_retain)
dot_file(mqtt_mosquitto__two_client_same_id)
dot_file(mqtt_mosquitto__non_clean)
dot_file(mqtt_mosquitto__mosquitto)
dot_file(mqtt_mosquitto__invalid)
dot_file(mqtt_emqtt__two_client_same_id)
dot_file(mqtt_emqtt__simple)
dot_file(mqtt_emqtt__non_clean)
dot_file(mqtt_emqtt__invalid)
dot_file(mqtt_VerneMQ__two_client_same_id)
dot_file(mqtt_VerneMQ__simple)
dot_file(mqtt_VerneMQ__non_clean)
dot_file(mqtt_VerneMQ__invalid)
dot_file(mqtt_ActiveMQ__two_client_will_retain)
dot_file(mqtt_VerneMQ__two_client_will_retain)
dot_file(edentifier2_learnresult_new_device-simple_fix)
dot_file(edentifier2_learnresult_old_device-simple_fix)
EOF
#dot_file(toyModels_lee_yannakakis_distinguishable)
#dot_file(toyModels_lee_yannakakis_non_distinguishable)
#dot_file(toyModels_naiks)
#dot_file(tls_JSSE_1.8.0_25_server_regular)
#dot_file(tls_JSSE_1.8.0_31_server_regular)
#dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)
#dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)
#dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)
#dot_file(benchmark_heating_system)
#EOF





#$skipConfigs && wantedconfig=()





useShortest="false"
drivers=(
'ConnexRandom(randomOutputs)'
#'Combined(Counter(5);ConnexRandom(randomOutputs))'
#'ConnexRandom(oneOutputDiff)'
)

usedStates=""
#usedStates+=" 5 10 15 20 30 50 70 100 150 200 300 500 700 1000 1500 2000 3000" #*1.5
usedStates+=" 5 15 30 70 150 300 700 1500 3000"
usedStates+=" 5 10 15 20 25 30 40 55 75 100 130 170 220 290 375 500 625 800 1000 1300 1700 2200 3000" #*1.3
usedStates+=" $(seq 40 20 220)"
# heuristic comparison
#for shortest in $useShortest; do
#    for driver in "${drivers[@]}" ; do
#        input=2
#        [ $driver = 'ConnexRandom(oneOutputDiff)' ] && input=5
#        for states in $usedStates; do
#            [ $driver = 'ConnexRandom(oneOutputDiff)' ] && [[ $states -gt 200 ]] && continue
#            [ $driver = 'Combined(Counter(5);ConnexRandom(randomOutputs))' ] && [[ $states -gt 200 ]] && continue
#
#            # without heuristic
#            wantedconfig+=("hW $states $driver $shortest $input none false false false false $useAdaptiveH $useAdaptiveW")
#
#            # h in W
#            wantedconfig+=("hW $states $driver $shortest $input none true false false false $useAdaptiveH $useAdaptiveW")
#
#            # only third inc and trace
#            #wantedconfig+=("hW $states $driver $shortest $input better false true false")
#
#            # third without trace
#            wantedconfig+=("hW $states $driver $shortest $input none false true false false $useAdaptiveH $useAdaptiveW")
#
#            # hzxw
#            wantedconfig+=("hW $states $driver $shortest $input none false false true false $useAdaptiveH $useAdaptiveW")
#
#            #all heuristics with trace
#            wantedconfig+=("hW $states $driver $shortest $input simple true true true false $useAdaptiveH $useAdaptiveW")
#            # all heuristics but not trace
#            wantedconfig+=("hW $states $driver $shortest $input none true true true false $useAdaptiveH $useAdaptiveW")
#
#        done;
#    done;
#done;

#wantedconfig=()

# adaptive comparison
useShortest="false"
drivers=(
'ConnexRandom(randomOutputs)'
'Combined(Counter(5);ConnexRandom(randomOutputs))'
'ConnexRandom(oneOutputDiff)'
)

usedStates=""
#usedStates+=" 5 10 15 20 30 50 70 100 150 200 300 500 700 1000 1500 2000 3000" #*1.5
#usedStates+=" 5 15 30 70 150 300 700 1500 3000"
#usedStates+=" 3000 2200 1700 1300 1000 800 625 500 375 290 220 170 130 100 75 55 40 30 25 20 15 10 5" #*1.3
#usedStates+=" 3000 2200 1700 1300 1000 800 625 500 375 290 220 170 130 100 75 55 40 30 25 20 15 10 5" #*1.3
#usedStates+=" $(seq 40 20 220)"
useAdaptiveHbak=$useAdaptiveH
useAdaptiveWbak=$useAdaptiveW
for shortest in $useShortest; do
    for driver in "${drivers[@]}" ; do
        input=2
        [ $driver = 'ConnexRandom(oneOutputDiff)' ] && input=5
        for states in $usedStates; do
            [[ $states -gt 1500 ]] && continue

            [ $driver = 'ConnexRandom(oneOutputDiff)' ] && [[ $states -gt 300 ]] && continue
            [ $driver = 'Combined(Counter(5);ConnexRandom(randomOutputs))' ] && [[ $states -gt 1000 ]] && continue
            useAdaptiveH=false
            useAdaptiveW=false

            # without heuristic
            #wantedconfig+=("hW $states $driver $shortest $input none false false false false $useAdaptiveH $useAdaptiveW")

            #all heuristics without trace without and h in W
            wantedconfig+=("hW $states $driver $shortest $input simple false true true false $useAdaptiveH $useAdaptiveW")

            useAdaptiveW=true

            [ $driver = 'ConnexRandom(oneOutputDiff)' ] && [[ $states -gt 100 ]] && continue
            [ $driver = 'Combined(Counter(5);ConnexRandom(randomOutputs))' ] && [[ $states -gt 150 ]] && continue
            [[ $states -gt 400 ]] && continue
            #all heuristics without trace without add h in W
            wantedconfig+=("hW $states $driver $shortest $input simple false true true false $useAdaptiveH $useAdaptiveW")
            #[[ $states -gt 300 ]] && continue
            #[ $driver = 'ConnexRandom(oneOutputDiff)' ] && [[ $states -gt 60 ]] && continue
            # without heuristic
            #wantedconfig+=("hW $states $driver $shortest $input none false false false false $useAdaptiveH $useAdaptiveW")
            [[ $states -gt 150 ]] && continue
            useAdaptiveH=true
            wantedconfig+=("hW $states $driver $shortest $input simple false true true false $useAdaptiveH $useAdaptiveW")
        done;
    done;
done;
useAdaptiveH=$useAdaptiveHbak
useAdaptiveW=$useAdaptiveWbak


drivers=(
'ConnexRandom(randomOutputs)'
#'Combined(Counter(5);ConnexRandom(randomOutputs))'
#'ConnexRandom(oneOutputDiff)'
)
#oracle comparison
usedStates=""
#usedStates+=" 5 10 15 20 30 50 70 100 150 200 300 500 700 1000 1500 2000 3000" #*1.5
#usedStates+=" 5 10 15 20 25 30 40 55 75 100 130 170 220 290 375 500 625 800 1000 1300 1700 2200 2900" #*1.3
useShortest="true false"
#for shortest in $useShortest; do
#    for driver in "${drivers[@]}" ; do
#        input=2
#        [ $driver = 'ConnexRandom(oneOutputDiff)' ] && input=5
#        for states in $usedStates; do
#            [ $driver = 'ConnexRandom(oneOutputDiff)' ] && [[ $states -gt 500 ]] && continue
#            #all heuristics together
#            wantedconfig+=("hW $states $driver $shortest $input simple true true true false $useAdaptiveH $useAdaptiveW")
#        done;
#    done;
#done;


# algorithm comparison
usedStates=""
#usedStates+=" 5 10 15 20 25 30 40 55 75 100 130 170 220 290 375 500 625 800 1000 1300 1700 2200" #*1.3
#usedStates+=" 3000"
usedStates+=" 5 10 15 20 25 30 40 55 75 100 130 170 220 " #*1.3
#usedStates+=" 5 10 15 20 30 50 75 100 125 150 175 200"
driver='ConnexRandom(randomOutputs)'
input=2
for states in $usedStates; do
    [[ $states -gt 220 ]] && continue
    [[ $states -le 40 ]] && wantedconfig+=("RS $states $driver $input true")
    [[ $states -le 100 ]] && wantedconfig+=("ZQ $states $driver $input false")
    # [[ $states -le 130 ]] && wantedconfig+=("ZQ $states $driver $input false") 
   [[ $states -le 500 ]] && wantedconfig+=("Lm $states $driver $input false") 
    [[ $states -le 100 ]] && wantedconfig+=("LocW $states $driver $input false")
    wantedconfig+=("hW $states $driver false $input simple true true true false $useAdaptiveH $useAdaptiveW")
    wantedconfig+=("hW $states $driver false $input simple false true true false $useAdaptiveH $useAdaptiveW")
    [[ $states -le 200 ]] && wantedconfig+=("hW $states $driver false $input simple false true true false true true")
done

driver='ConnexRandom(oneOutputDiff)'
input=5
#$skipConfigs&&usedStates=5
for states in $usedStates; do
    [[ $states -gt 170 ]] && continue
    [[ $states -le 100 ]] && wantedconfig+=("RS $states $driver $input true")
    [[ $states -le 55 ]] && wantedconfig+=("ZQ $states $driver $input false") 
    [[ $states -le 1000 ]] && wantedconfig+=("Lm $states $driver $input false") 
    [[ $states -le 70 ]] && wantedconfig+=("LocW $states $driver $input false")
    [[ $states -le 1500 ]] && wantedconfig+=("hW $states $driver false $input simple false true true false false false")
    [[ $states -le 200 ]] && wantedconfig+=("hW $states $driver false $input simple false true true false true true")
    #[[ $states -le 500 ]] && wantedconfig+=("hW $states $driver false $input simple true true true false false false")
done

#algorithms comprison on inputs
driver='ConnexRandom(randomOutputs)'
states=30
inputs="2 4 6 5 8 10 15 20 25 30 35 40 45 50 55 60 70 80"
inputs="2 10 20 30 40 50 60 70 80"
#$skipConfigs&&inputs="2"
for input in $inputs;do
    [[ $input -le 10 ]] && wantedconfig+=("RS $states $driver $input true") 
#   [[ $input -le 150 ]] && wantedconfig+=("RS $states $driver $input false")
    [[ $input -le 10 ]] && wantedconfig+=("ZQ $states $driver $input false") 
    [[ $input -le 100 ]] && wantedconfig+=("Lm $states $driver $input false") 
    $skipConfigs||([[ $input -le 30 ]] && wantedconfig+=("LocW $states $driver $input false") )
    [[ $input -le 30 ]] && wantedconfig+=("LocW $states $driver $input false")
    [[ $input -le 100 ]] && wantedconfig+=("hW $states $driver false $input simple false true true false false false")
    [[ $input -le 100 ]] && wantedconfig+=("hW $states $driver false $input simple false true true false true true")
done


#test inputs
usedStates="10 30 60"
usedStates="30 60"
usedStates="30"
drivers='ConnexRandom(randomOutputs)'
useShortest='false'
inputs="2 4 6 5 8 10 15 20 25 30 35 40 45 50 55 60"
for shortest in $useShortest; do
    for s in $usedStates; do
        for input in $inputs;do
            states=$(($s))
            for driver in $drivers ; do
                #wantedconfig+=("hW $states $driver $shortest $input none false false false false $useAdaptiveH $useAdaptiveW")
                for adaptive in "true" "false"
                do
                    wantedconfig+=("hW $states $driver $shortest $input simple false true true false $adaptive $adaptive")
                done
            done
        done;
    done;
done;


shopt -s expand_aliases
alias java-simp="/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10000M -Dfile.encoding=UTF-8 -classpath $simpa_dir/hit/bin:$simpa_dir/hit/lib/weka.jar:$simpa_dir/hit/lib/sipunit/concurrent-1.3.3.jar:$simpa_dir/hit/lib/sipunit/dom-2.3.0-jaxb-1.0.6.jar:$simpa_dir/hit/lib/sipunit/isorelax-20030108.jar:$simpa_dir/hit/lib/sipunit/jain-sip-api-1.2.1.jar:$simpa_dir/hit/lib/sipunit/jain-sip-ri-1.2.164.jar:$simpa_dir/hit/lib/sipunit/jax-qname-1.1.jar:$simpa_dir/hit/lib/sipunit/jaxb-api-1.0.1.jar:$simpa_dir/hit/lib/sipunit/jaxb-impl-1.0.3.jar:$simpa_dir/hit/lib/sipunit/jaxb-libs-1.0.3.jar:$simpa_dir/hit/lib/sipunit/jaxb-xjc-1.0.7.jar:$simpa_dir/hit/lib/sipunit/junit-4.8.2.jar:$simpa_dir/hit/lib/sipunit/log4j-1.2.8.jar:$simpa_dir/hit/lib/sipunit/namespace-1.0.1.jar:$simpa_dir/hit/lib/sipunit/nist-sdp-1.0.jar:$simpa_dir/hit/lib/sipunit/relaxngDatatype-2011.1.jar:$simpa_dir/hit/lib/sipunit/sipunit-2.0.0.jar:$simpa_dir/hit/lib/sipunit/stun4j-1.0.MOBICENTS.jar:$simpa_dir/hit/lib/sipunit/xsdlib-20060615.jar:$simpa_dir/hit/lib/htmlunit/htmlunit-2.11.jar:$simpa_dir/hit/lib/htmlunit/httpclient-4.2.2.jar:$simpa_dir/hit/lib/htmlunit/commons-codec-1.7.jar:$simpa_dir/hit/lib/htmlunit/commons-collections-3.2.1.jar:$simpa_dir/hit/lib/htmlunit/commons-io-2.4.jar:$simpa_dir/hit/lib/htmlunit/commons-lang3-3.1.jar:$simpa_dir/hit/lib/htmlunit/commons-logging-1.1.1.jar:$simpa_dir/hit/lib/htmlunit/cssparser-0.9.8.jar:$simpa_dir/hit/lib/htmlunit/htmlunit-core-js-2.11.jar:$simpa_dir/hit/lib/htmlunit/httpcore-4.2.2.jar:$simpa_dir/hit/lib/htmlunit/httpmime-4.2.2.jar:$simpa_dir/hit/lib/htmlunit/nekohtml-1.9.17.jar:$simpa_dir/hit/lib/htmlunit/sac-1.3.jar:$simpa_dir/hit/lib/htmlunit/serializer-2.7.1.jar:$simpa_dir/hit/lib/htmlunit/xalan-2.7.1.jar:$simpa_dir/hit/lib/htmlunit/xercesImpl-2.10.0.jar:$simpa_dir/hit/lib/htmlunit/xml-apis-1.4.01.jar:$simpa_dir/hit/lib/json/jackson-core-asl-1.9.11.jar:$simpa_dir/hit/lib/json/jackson-mapper-asl-1.9.11.jar:$simpa_dir/hit/lib/jsoup-1.8.2.jar:$simpa_dir/hit/lib/antlr-4.5.3-complete.jar main.simpa.SIMPA"


pushd "$simpa_dir/hit" > /dev/null
#for states in 5 10 15 20 30 40 50 60 70 80 90 100 120 140
#for states in 100 120 140
#do
    #date
    #states=$(($states+5))
    #java-simp drivers.mealy.transparent.RandomMealyDriver --mininputsym 2 --maxinputsym 2 --minoutputsym 2 --maxoutputsym 2 --minstates $states --maxstates $states --stats --nbtest 10 --rivestSchapire --RS-probabilistic
    #java-simp drivers.mealy.transparent.RandomMealyDriver --mininputsym 2 --maxinputsym 2 --minoutputsym 2 --maxoutputsym 2 --minstates $states --maxstates $states --stats --nbtest 10 --rivestSchapire --RS-probabilistic
#done


states=10
#states=30
#for input in 6 10 20 50
#do
#    date
#    java-simp drivers.mealy.transparent.RandomMealyDriver --mininputsym $input --maxinputsym $input --minoutputsym 2 --maxoutputsym 2 --minstates $states --maxstates $states --stats --nbtest 10 --rivestSchapire --RS-probabilistic
#done

#for states in $(seq 1 11)
#do
#pushd "$simpa_dir/hit"
#java-simp drivers.mealy.transparent.RandomMealyDriver --mininputsym 2 --maxinputsym 2 --minoutputsym 2 --maxoutputsym 2 --minstates $states --maxstates $states --stats --nbtest $testPerLoop --cutCombinatorial
#popd
#done
popd > /dev/null


RESET='\e[0m' # No Color
WHITE='\e[1;37m'
BLACK='\e[0;30m'
BLUE='\e[0;34m'
LIGHT_BLUE='\e[1;34m'
GREEN='\e[0;32m'
LIGHT_GREEN='\e[1;32m'
CYAN='\e[0;36m'
LIGHT_CYAN='\e[1;36m'
RED='\e[0;31m'
LIGHT_RED='\e[1;31m'
PURPLE='\e[0;35m'
LIGHT_PURPLE='\e[1;35m'
BROWN='\e[0;33m'
YELLOW='\e[1;33m'
GRAY='\e[0;30m'
LIGHT_GRAY='\e[0;37m'


max=0;
sum=0;
nbCase=0;
min=$limit

function getNb(){
    learner=$1
    shift
    states=$1
    driver=$2

    outputNb='[^,]*'
    if [ "$driver" = 'ConnexRandom(randomOutputs)' ]
    then
        outputNb=2
    fi
    if echo "$driver" | grep '^dot_file'>/dev/null
    then
	states='[^,]*'
        outputNb='[^,]*'
    fi
    if [ $learner = "hW" ]
    then
        csvfile="$csv_dir/learner.mealy.hW.HWStatsEntry.csv"
        shortest=$($3 && echo 'shortest' || echo 'MrBean')
        inputNb=$4
        tryTrace=$5
        hInW=$6
        third_inconsistency=$7
        hzxw=$8
        knownW=$9
        adaptiveH=${10}
        adaptiveW=${11}
        if echo "$driver" | grep '^dot_file'>/dev/null
        then
            inputNb='[^,]*'
        fi

        cat "$csvfile" | cut -d, -f8,9,10,12,22,26,27,28,29,30,31,32 | grep "^$inputNb,$outputNb,$states,$driver,$shortest,$tryTrace,$hInW,$third_inconsistency,$hzxw,$knownW,$adaptiveH,$adaptiveW" | wc -l
    elif [ $learner = "RS" ]
    then
        inputNb=$3
        oracle="MrBean"
        hIsKnown=false
        if echo "$driver" | grep '^dot_file'>/dev/null
        then
            inputNb='[^,]*'
        fi

        csvfile="$csv_dir/learner.mealy.rivestSchapire.RivestSchapireStatsEntry.csv"
        cat "$csvfile" | cut -d, -f5,6,7,9,12,15 | grep "^$inputNb,$outputNb,$states,$driver,$hIsKnown,$oracle" | wc -l

    elif [ $learner = "LocW" ]
    then
        inputNb=$3
        if echo "$driver" | grep '^dot_file'>/dev/null
        then
            inputNb='[^,]*'
        fi
        speedUp=false;
        wSize=2
        csvfile="$csv_dir/learner.mealy.localizerBased.LocalizerBasedStatsEntry.csv"
        cat "$csvfile" | cut -d, -f1,6,7,8,11,14 | grep "^$wSize,$inputNb,$outputNb,$states,$driver,$speedUp" | wc -l
    elif [ $learner = "ZQ" ]
    then
        inputNb=$3
        if echo "$driver" | grep '^dot_file'>/dev/null
        then
            inputNb='[^,]*'
        fi
        oracle="Unknown"
        csvfile="$csv_dir/learner.mealy.tree.ZStatsEntry.csv"
        cat "$csvfile" | cut -d, -f2,3,4,6,10 | grep "^$inputNb,$outputNb,$states,$driver,$oracle" | wc -l
    elif [ $learner = "Lm" ]
    then
        inputNb=$3
        if echo "$driver" | grep '^dot_file'>/dev/null
        then
            inputNb='[^,]*'
        fi
        oracle="Unknown"
        csvfile="$csv_dir/learner.mealy.table.LmStatsEntry.csv"
        cat "$csvfile" | cut -d, -f2,3,4,6,10 | grep "^$inputNb,$outputNb,$states,$driver,$oracle" | wc -l
    else
        echo "error:invalid learner"
        sleep 10
    fi
}



#for line in "${wantedconfig[@]}"
#do
#    echo $line
#    echo $(getNb $line)
#    echo
#done



lower=false




for line in "${wantedconfig[@]}"
do
    count=$(getNb $line)
    sum=$(($sum+$count))
    nbCase=$(($nbCase+1))
    if [[ $count -ge $max ]]
    then
        max=$count
#        echo newmax $max states=$states driver=$driver shortest=$shortest
    else
        lower=true
    fi
    if [[ $count -lt $min ]]
    then
        min=$count
    fi
done;

avg=$(($sum/$nbCase))

echo
echo "min is $min"
echo "max is $max"
echo "avg is $avg"
echo

if [[ $(($min+$testPerLoop)) -gt $max ]]
then
    if $lower
    then
        echo ''
    else
        echo 'increasing max'
        max=$(($max+$testPerLoop))
    fi
else
    max=$(($min+$testPerLoop))
    echo "decreasing max to $max"
fi




if [[ $max -gt $limit ]]
then
    max=$limit
fi
if [[ $min -ge $limit ]]
then
    echo -e "${GREEN}LIMIT $limit REACHED$RESET"
    echo "exiting"
    # next part of algorithm is used to cut head of CSV files when there was an error in the CSV files
    hWcsvFile="$csv_dir/learner.mealy.hW.HWStatsEntry.csv"
    RScsvFile="$csv_dir/learner.mealy.rivestSchapire.RivestSchapireStatsEntry.csv"
    locWcsvFile="$csv_dir/learner.mealy.rivestSchapire.RivestSchapireStatsEntry.csv"
    lmCSVFile="$csv_dir/learner.mealy.table.LmStatsEntry.csv"
    if grep -n -e '16584,9,40,278541,2,2,55,0,ConnexRandom(randomOutputs),6.878719,1910904928,false,0,0,MrBean,202,' "$RScsvFile"
    then
        echo -e "$RED WRONG data still in file RS ...$RESET"
        cp "$RScsvFile" "/home/nbremond/bak/csv.bak$(date +%s)"
        head -n1 $RScsvFile > $RScsvFile.tail
        tail -n +50 "$RScsvFile" >> "$RScsvFile.tail"
        mv "$RScsvFile.tail" "$RScsvFile"
        echo -n "entries in csv file :"
        cat "$RScsvFile" | wc -l
    elif grep -n '182027,13,10,36,51,dot_file(TCP_Windows8_Server__connected),1.2828492,-1749000929702174179,6,Unknown,0,1.2069933,20167,' "$lmCSVFile"
    then
        echo -e "$RED WRONG data still in file Lm ...$RESET"
        cp "$lmCSVFile" "/home/nbremond/bak/csv.bak$(date +%s)"
        head -n1 $lmCSVFile > $lmCSVFile.tail
        tail -n +100 "$lmCSVFile" >> "$lmCSVFile.tail"
        mv "$lmCSVFile.tail" "$lmCSVFile"
        echo -n "entries in csv file :"
        cat "$lmCSVFile" | wc -l
    elif grep -n '2,1,20,8,249,5,4,4,4,55,dot_file(edentifier2_learnresult_old_device-simple_fix),0.001836842,5359512,false,2,' "$locWcsvFile"
    then
        echo -e "$RED WRONG data still in file locW ...$RESET"
        cp "$locWcsvFile" "/home/nbremond/bak/csv.bak$(date +%s)"
        head -n1 $locWcsvFile > $locWcsvFile.tail
        tail -n +100 "$locWcsvFile" >> "$locWcsvFile.tail"
        mv "$locWcsvFile.tail" "$locWcsvFile"
        echo -n "entries in csv file :"
        cat "$locWcsvFile" | wc -l
    elif grep -n '6,37,19,19,498,55670,2429985,2,2,2200,0,ConnexRandom(randomOutputs),25.190203,2142736488,-1,2200,8,6240348686508564927,2,19,9,MrBean,2,0.024053097,31,simple,true,true,true,false,' "$hWcsvFile"
    then
        echo -e "$RED WRONG data still in file hW ...$RESET"
        cp "$hWcsvFile" "/home/nbremond/bak/csv.bak$(date +%s)"
        head -n1 $hWcsvFile > $hWcsvFile.tail
        tail -n +500 "$hWcsvFile" >> "$hWcsvFile.tail"
        mv "$hWcsvFile.tail" "$hWcsvFile"
        echo -n "entries in csv file :"
        cat "$hWcsvFile" | wc -l
    elif [ -e "$skipConfigFile" ]
    then
        rm "$skipConfigFile" 2> /dev/null
    else
        echo 'csv files seems clean'
        sleep 10
        ## the next lines can be uncommented to put computer in sleep mode after making stats
        #systemctl suspend
        #sleep 120
    fi
    exit 0
fi




echo
echo "starting inferences"
echo
for line in "${wantedconfig[@]}"
do
    if [ "$(stat -c %Y $0)" != "$scriptTime" ]
    then
        echo -e "${GREEN}script was modified, aborting$RESET"
        sleep 5
        break
    fi
    count=$(getNb $line)
    learner="$(echo "$line" | cut -d' ' -f1)"
    states="$(echo "$line" | cut -d' ' -f2)"
    driver="$(echo "$line" | cut -d' ' -f3)"
    if [[ $max -gt $count ]]
    then
        nbtest=$testPerLoop
        if [[ $(($count+$nbtest)) -gt $max ]]
        then
            nbtest=$(($max-$count))
        fi

        if [ "$learner" = "hW" ]
        then
            shortest="$(echo "$line" | cut -d' ' -f4)"
            inputNb="$(echo "$line" | cut -d' ' -f5)"
            tryTrace="$(echo "$line" | cut -d' ' -f6)"
            hInW="$(echo "$line" | cut -d' ' -f7)"
            third_inc="$(echo "$line" | cut -d' ' -f8)"
            hzxw="$(echo "$line" | cut -d' ' -f9)"
            knownW="$(echo "$line" | cut -d' ' -f 10)"
            adaptiveH="$(echo "$line" | cut -d' ' -f 11)"
            adaptiveW="$(echo "$line" | cut -d' ' -f 12)"
            options=$($shortest && echo '--shortestCE')" "$( ([ "$tryTrace" = 'naive' ] || [ "$tryTrace" = 'simple' ]) && echo '--tryTraceCE')"  "$($knownW && echo '--hW-with-known-W')"  "$($hInW && echo '--addHInW')" "$($adaptiveW && echo '--adaptive-w-seq')" "$($adaptiveH && echo '--adaptive-h')" "$($hzxw && echo '--hzxw')" "$($third_inc && echo '--3rd-inconsistency')' '--hW

        elif [ "$learner" = "RS" ]
        then
            inputNb="$(echo "$line" | cut -d' ' -f4)"
            ceLength=$(($states))
            ceResets=$(($states*$inputNb*100))
            if [ $inputNb = '_' ] || [ $states = '_' ]
            then
                ceLength=40
                ceResets=100000
            fi
            options="--RS-probabilistic --rivestSchapire"
            options+=" --maxcelength $ceLength --maxceresets $ceResets"

        elif [ "$learner" = "Lm" ]
        then
            inputNb="$(echo "$line" | cut -d' ' -f4)"
            ceLength=$(($states))
            ceResets=$(($states*$inputNb*100))
            if [ $inputNb = '_' ] || [ $states = '_' ]
            then
                ceLength=40
                ceResets=100000
            fi
            options="--lm"
            options+=" --maxcelength $ceLength --maxceresets $ceResets"

        elif [ "$learner" = "ZQ" ]
        then
            inputNb="$(echo "$line" | cut -d' ' -f4)"
            ceLength=$(($states))
            ceResets=$(($states*$inputNb*100))
            if [ $inputNb = '_' ] || [ $states = '_' ]
            then
                ceLength=40
                ceResets=100000
            fi
            options="--tree -I=X"
            options+=" --maxcelength $ceLength --maxceresets $ceResets"
        elif [ "$learner" = "LocW" ]
        then
            #nbtest=$((testPerLoop*3))
            #if [[ $(($count+$nbtest)) -gt $limit ]]
            #then
            #    nbtest=$(($max-$count))
            #fi
            inputNb="$(echo "$line" | cut -d' ' -f4)"
            options="--localizerBased"
            options+=" --noSpeedUp"
        else
            echo "wrong line $line"
            sleep 10
        fi
        echo "max is $max and there is only $count results for $states states and driver $driver and shortest=$shortest and $inputNb inputs"
        r_states=$states
        case "$driver" in
            'ConnexRandom(randomOutputs)')
                drivername=drivers.mealy.transparent.RandomMealyDriver
                ;;

            'ConnexRandom(oneOutputDiff)')
                drivername=drivers.mealy.transparent.RandomOneOutputDiffMealyDriver
                ;;
            'Combined(Counter(5);ConnexRandom(randomOutputs))')
                drivername=drivers.mealy.transparent.RandomAndCounterMealyDriver
                r_states=$(($states/3))
                ;;
                     'dot_file(TCP_Windows8_Server__connected)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mod/TCP_Windows8_Server.dot'
    inputNb=1
    r_states=1
    ;;
                     'dot_file(edentifier2_learnresult_new_device-simple_fix)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/edentifier2/learnresult_new_device-simple_fix.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(edentifier2_learnresult_old_device-simple_fix)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/edentifier2/learnresult_old_device-simple_fix.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_unknown1)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/unknown1.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_ActiveMQ__invalid)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/ActiveMQ__invalid.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_ActiveMQ__non_clean)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/ActiveMQ__non_clean.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_mosquitto__two_client_will_retain)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/mosquitto__two_client_will_retain.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_analyse)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/analyse.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_mosquitto__two_client_same_id)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/mosquitto__two_client_same_id.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_mosquitto__non_clean)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/mosquitto__non_clean.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_mosquitto__mosquitto)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/mosquitto__mosquitto.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_mosquitto__invalid)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/mosquitto__invalid.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_emqtt__two_client_same_id)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/emqtt__two_client_same_id.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_emqtt__simple)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/emqtt__simple.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_emqtt__non_clean)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/emqtt__non_clean.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_emqtt__invalid)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/emqtt__invalid.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_VerneMQ__two_client_same_id)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/VerneMQ__two_client_same_id.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_VerneMQ__simple)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/VerneMQ__simple.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_VerneMQ__non_clean)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/VerneMQ__non_clean.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_VerneMQ__invalid)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/VerneMQ__invalid.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_ActiveMQ__two_client_will_retain)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/ActiveMQ__two_client_will_retain.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(mqtt_VerneMQ__two_client_will_retain)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/mqtt/VerneMQ__two_client_will_retain.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(tls_JSSE_1.8.0_25_server_regular)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/tls/JSSE_1.8.0_25_server_regular.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(tls_JSSE_1.8.0_31_server_regular)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/tls/JSSE_1.8.0_31_server_regular.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(toyModels_naiks)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/toyModels/naiks.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(toyModels_lee_yannakakis_distinguishable)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/toyModels/lee_yannakakis_distinguishable.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(toyModels_lee_yannakakis_non_distinguishable)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/toyModels/lee_yannakakis_non_distinguishable.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/test/1/out/dot_file(edentifier2_learnresult_old_device-simple_fix)_inf.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/test/2/out/dot_file(edentifier2_learnresult_old_device-simple_fix)_inf.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/test/3/out/dot_file(edentifier2_learnresult_old_device-simple_fix)_inf.dot'
    inputNb=1
    r_states=1
    ;;
    
     'dot_file(benchmark_heating_system)')
    drivername=drivers.mealy.transparent.FromDotMealyDriver
    options+=' --loadDotFile /home/nbremond/Téléchargements/benchmark/heating_system.dot --maxcelength 1000'
    inputNb=1
    r_states=1
    ;;


	    *)
                echo -e "$RED unknown driver name$RESET"
                sleep 5
                exit 1
                ;;
        esac
        pushd "$simpa_dir/hit" > /dev/null
        date
        java-simp $drivername --mininputsym $inputNb --maxinputsym $inputNb --minoutputsym 2 --maxoutputsym 2 --minstates $r_states --maxstates $r_states --stats --nbtest $nbtest $options || sleep 5
        popd >/dev/null
    else
        echo "max is $max and there is already $count results for $learner with $states states and driver $driver"
    fi
done;

if $makeGraph
then
    pushd "$simpa_dir/hit"
    java-simp drivers.mealy.transparent.RandomMealyDriver --mininputsym 2 --maxinputsym 2 --minoutputsym 2 --maxoutputsym 2 --minstates 1 --maxstates 1 --stats --nbtest 0 --hW --makeGraph
    popd
fi

echo "max was $max"
