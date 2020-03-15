#/bin/sh

REPOSITORY="sensinact"
TAG="latest"

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
	    *) 
	    ;;
	esac
done
sudo docker run -t -i $REPOSITORY:$TAG