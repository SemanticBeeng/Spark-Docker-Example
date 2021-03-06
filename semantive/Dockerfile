# Original version: https://github.com/Semantive/docker-spark/blob/master/Dockerfile
# Build with
# docker build -t "semantive/spark2" semantive/
#
FROM openjdk:8-jre
MAINTAINER Semantive "https://github.com/semantive"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y --no-install-recommends apt-utils

RUN apt-get install -y locales \
 && dpkg-reconfigure -f noninteractive locales \
 && locale-gen C.UTF-8 \
 && /usr/sbin/update-locale LANG=C.UTF-8 \
 && echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
 && locale-gen

# Users with other locales should set this in their derivative image
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# APT cleanup
RUN rm -rf /var/lib/apt/lists/*

# Create user for Spark
RUN useradd -ms /bin/bash spark
RUN useradd -ms /bin/bash hadoop

# HADOOP
ENV HADOOP_VERSION 2.7.3
ENV HADOOP_HOME /opt/hadoop-$HADOOP_VERSION
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV PATH $PATH:$HADOOP_HOME/bin
ENV LD_LIBRARY_PATH $LD_LIBRARY_PATH:$HADOOP_HOME/lib/native
RUN wget -q -O- --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 0 \
  "http://archive.apache.org/dist/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz" \
  | tar -xz -C /opt/ \
 && rm -rf $HADOOP_HOME/share/doc \
 && chown -R hadoop:hadoop $HADOOP_HOME

# SPARK
ENV SPARK_VERSION 2.1.0
ENV SPARK_PACKAGE spark-${SPARK_VERSION}-bin-hadoop2.7
ENV SPARK_HOME /home/spark
ENV SPARK_DIST_CLASSPATH="$HADOOP_HOME/etc/hadoop/*:$HADOOP_HOME/share/hadoop/common/lib/*:$HADOOP_HOME/share/hadoop/common/*:$HADOOP_HOME/share/hadoop/hdfs/*:$HADOOP_HOME/share/hadoop/hdfs/lib/*:$HADOOP_HOME/share/hadoop/hdfs/*:$HADOOP_HOME/share/hadoop/yarn/lib/*:$HADOOP_HOME/share/hadoop/yarn/*:$HADOOP_HOME/share/hadoop/mapreduce/lib/*:$HADOOP_HOME/share/hadoop/mapreduce/*:$HADOOP_HOME/share/hadoop/tools/lib/*"
ENV PATH $PATH:${SPARK_HOME}/bin
RUN wget -q -O- --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 0 \
  "http://mirrors.advancedhosters.com/apache/spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz" \
  | tar xz --strip 1 -C $SPARK_HOME/ \
 && chown -R spark:spark $SPARK_HOME

# Install the Mysql Jdbc connector
RUN wget -P/home/spark/jars \
  "http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.38/mysql-connector-java-5.1.38.jar"

USER root
WORKDIR $SPARK_HOME
CMD ["su", "-c", "bin/spark-class org.apache.spark.deploy.master.Master", "spark"]
