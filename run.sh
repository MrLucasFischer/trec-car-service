java -Xmn100M  -XX:+PrintGCDetails  -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40\
     -Dcom.sun.management.jmxremote.port=5050 \
     -Dcom.sun.management.jmxremote.rmi.port=5050 \
     -Dcom.sun.management.jmxremote.host=localhost \
     -Djava.rmi.server.hostname=locahost \
     -cp target/trec-car-service-1.0-SNAPSHOT-jar-with-dependencies.jar Server /scratch/fmartins/paragraphIndex/