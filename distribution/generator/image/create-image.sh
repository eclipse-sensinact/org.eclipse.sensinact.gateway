#/bin/sh

function isPomOrSkeleton (){	
    if [[ ( "$1" = "../profile/pom.xml" ) || ( "$1" = "../profile/ZprofileSkeleton") ]] 
    then
	 return 0
	else
	 return 1
	fi;
}

CURRENT_DIR=`(pwd)`
export SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $SCRIPT_DIRECTORY
cp ../packaging/target/*.zip ./sensiNact-gateway-latest.zip
if test -f "./sensiNact-gateway-latest.zip"; then
    echo "./sensiNact-gateway-latest.zip exists"
else
    echo "./sensiNact-gateway-latest.zip doest not exist"
	exit 0
fi

REPOSITORY="sensinact"
TAG="latest"
PORT="8080"
CONFIG="./#MYCONFIG-FILE#"
FOLDER="./#MYCONFIG-FOLDER#"
MODULE="./#JAR-FILE#"
EXTRA="./#JAR-FOLDER#"

while [[ $# -gt 0 ]]
do
	key="$1"	
	case $key in
	    -r|--repo)
		    REPOSITORY="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    -t|--tag)
		    TAG="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    -p|--port)
		    PORT="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    -c|--config)
		    CONFIG="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    -f|--config-folder)
		    FOLDER="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    -m|--module)
		    MODULE="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    -e|--extra-folder)
		    EXTRA="$2"
		    shift # past argument
		    shift # past value
	    ;;
	    *) 
	    ;;
	esac
done

rm Dockerfile
i=0

for f in ../profile/*;
do
	if ! isPomOrSkeleton "$f" ;	then
	    title=`awk -v n=2 '/<artifactId>([^<]+)<\/artifactId>/ {l++}(l==n) {print}' $f/pom.xml | \
	    awk 'NR==1{print $1}' | \
	    sed -e 's/<artifactId>//g'| \
	    sed -e 's/<\/artifactId>//g'`	
		state="off"	
		files[i]=$(echo -en " ")
		files[i+1]="$title $f $state"
		((i+=2))
	fi
done
result=$(whiptail --title "sensiNact Image Profile Configurator" --checklist "Please select the profiles to be activated" 36 70 26 ${files[@]} 2>&1 >/dev/tty)
			
if [ $? == 1 ]; then
	echo "Cancelling..."
exit 0
fi

echo "FROM ubuntu:latest" > Dockerfile
echo "ENV DEBIAN_FRONTEND noninteractive" >> Dockerfile
echo "RUN apt-get update" >> Dockerfile
echo "RUN apt-get install -y apt-utils curl iptables telnet libarchive-tools sqlite3 openjdk-8-jre" >> Dockerfile
echo "WORKDIR /opt" >> Dockerfile
echo "RUN mkdir sensiNact" >> Dockerfile
echo "ADD ./sensiNact-gateway-latest.zip /opt/sensiNact" >> Dockerfile
echo "WORKDIR /opt/sensiNact" >> Dockerfile
echo "RUN bsdtar -xf ./sensiNact-gateway-latest.zip -s'|[^/]*/||'" >> Dockerfile
echo "RUN chmod +x sensinact" >> Dockerfile

if test -f "$CONFIG"; then
    echo "ADD $CONFIG /opt/sensiNact/cfgs/" >> Dockerfile
fi

if [ -d "$FOLDER" ]; then
    for f in $FOLDER/*;
	do
		if test -f "$f"; then
			echo "ADD $f /opt/sensiNact/cfgs/" >> Dockerfile
		fi
	done    
fi

for f in $result
do
    dir=`sed -e 's/"//g' <<< $f`
    echo "RUN cp -vf load/$dir/*.jar bundle/" >> Dockerfile
done

if test -f "$MODULE"; then
    if [[ $MODULE =~ (.+\.jar) ]]; then 
	   echo "ADD $MODULE /opt/sensiNact/bundle/" >> Dockerfile
	fi
fi

if [ -d "$EXTRA" ]; then
    for f in $EXTRA/*;
	do
		if [[ $f =~ (.+\.jar) ]]; then 
			echo "ADD $f /opt/sensiNact/bundle/" >> Dockerfile
		fi
	done    
fi

echo "RUN echo \"org.eclipse.sensinact.simulated.gui.enabled=false\" >> conf/config.properties" >> Dockerfile
echo "RUN echo \"org.osgi.service.http.port=$PORT\" >> conf/config.properties" >> Dockerfile
echo "EXPOSE $PORT" >> Dockerfile
echo "ENTRYPOINT [\"/opt/sensiNact/sensinact\"]" >> Dockerfile
echo "Creating $REPOSITORY:$TAG"
sudo docker build . -t $REPOSITORY:$TAG
rm ./sensiNact-gateway-latest.zip
#rm Dockerfile
cd $CURRENT_DIR
