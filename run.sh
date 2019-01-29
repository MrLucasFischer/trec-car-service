java -Xmn100M  -XX:+PrintGCDetails  -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40\
     -Dcom.sun.management.config.file=/opt/app/management.properties \
     -Djava.util.logging.config.file=/opt/app/logging.properties \
     -Dcom.sun.management.jmxremote.port=5050 \
     -Dcom.sun.management.jmxremote.rmi.port=5050 \
     -Dcom.sun.management.jmxremote.host=localhost \
     -Djava.rmi.server.hostname=locahost \
     -cp trec-car-service-1.0-SNAPSHOT-jar-with-dependencies.jar Server /scratch/fmartins/paragraphIndex/