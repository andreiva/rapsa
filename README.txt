
How to run

//	sudo java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address="8000" -cp .:/opt/pi4j/lib/pi4j-core.jar bmp085/test085
// sudo java -classpath .:classes:/opt/pi4j/lib/'*' SerialExample






# remote debugging, 
# almost work, complains about wrong architecture

sudo java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address="8000" -jar main-1.0-SNAPSHOT.jar


# just run application
sudo java -jar main-1.0-SNAPSHOT.jar

