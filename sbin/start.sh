###################### call run-class.sh   ######################
export APP_HOME=$(
  cd $(dirname $0)/..
  pwd
)
export LOG_DIR=/var/log/arc-face
export CLASSPATH_LOCATION=${APP_HOME}/libs
export IMAGE_LOCATION=${APP_HOME}/image
export SO_LOCATION=${APP_HOME}/libc/LINUX64


export JMEDIA_OPTS=" -Djni.library.path=${SO_LOCATION} -Ddir.face.db=${IMAGE_LOCATION}/db -Ddir.face.check=${IMAGE_LOCATION}/check "
#export JMEDIA_HEAP_OPTS=" -Xmx${xmx} -Xms${xms} -XX:MaxDirectMemorySize=${MaxDirectMemorySize}"
#export JMEDIA_JVM_PERFORMANCE_OPTS=" -server -XX:SurvivorRatio=8 -XX:+UseParallelGC -XX:+UseParallelOldGC  -XX:+UseAdaptiveSizePolicy -XX:+PrintAdaptiveSizePolicy -XX:MaxGCPauseMillis=50 -Djava.awt.headless=true "
#export JMEDIA_NETTY_OPTS=" -Dio.netty.allocator.type=pooled -Dio.netty.noPreferDirect=false -Dio.netty.leakDetection.level=DISABLED"

EXTRA_ARGS="-loggc "
COMMAND=$1
case $COMMAND in
-h)
  echo "USAGE: $0 [-daemon]"
  exit 1
  ;;
-daemon)
  EXTRA_ARGS="-daemon "
  shift
  ;;
*) ;;

esac

exec sh -x "${APP_HOME}"/bin/run-class.sh -name arc-face ${EXTRA_ARGS} com.las.demo.arc.face.FaceEngineTest
