# Classpath Capture

When Oozie executes a Java action, it adds various jars to the class path.

I was asked to troubleshoot an issue where a Java action runs fine when run via `java -jar some-jar.jar` and throws an error when executed as an Oozie Java action.

This creates a jar that writes the group, artifact, and version of all the jars in the classpath.

## Usage

Create a `workflow.xml` file:

    <workflow-app name="WorkflowJavaMainAction" xmlns="uri:oozie:workflow:0.1">
        <start to="classpathCaptureAction"/>
         <action name="classpathCaptureAction">
            <java>
                <job-tracker>${jobTracker}</job-tracker>
                <name-node>${nameNode}</name-node>
                <configuration>
                    <property>
                        <name>mapred.job.queue.name</name>
                        <value>${queueName}</value>
                    </property>
                </configuration>
                <main-class>io.woolford.Main</main-class>
            </java>
            <ok to="end"/>
            <error to="killJobAction"/>
        </action>
        <kill name="killJobAction">
            <message>"Killed job due to error: ${wf:errorMessage(wf:lastErrorNode())}"</message>
        </kill>
        <end name="end" />
    </workflow-app>

Create a `job.properties` file:

    nameNode=hdfs://node1.support.com:8020
    jobTracker=node3.support.com:8050
    queueName=default
    
    oozie.libpath=${nameNode}/user/oozie/share/lib
    oozie.use.system.libpath=true
    oozie.wf.rerun.failnodes=true
    
    oozieProjectRoot=${nameNode}/apps/oozie_jobs/classpath-capture
    appPath=${oozieProjectRoot}
    oozie.wf.application.path=${appPath}

Build the jar:

    mvn clean package

Create this directory structure in HDFS:

    /apps/oozie_jobs/classpath-capture
    /apps/oozie_jobs/classpath-capture/lib
    /apps/oozie_jobs/classpath-capture/lib/classpath-capture-1.0-SNAPSHOT.jar
    /apps/oozie_jobs/classpath-capture/workflow.xml

Run the Oozie job:

    oozie job -oozie http://node3.support.com:11000/oozie -config job.properties -run

This Java action will create a file, `/tmp/classpath-capture.txt`, on the datanode where it's run. I usually `cat` this file from Ansible to avoid having to look for it:

    ansible mycluster -m shell -a 'cat /tmp/classpath-capture.txt'
    
The output will contain the group, artifact, and version of all the dependencies in the classpath, e.g.:

    org.apache.hadoop:hadoop-client:2.7.3
    org.apache.hadoop:hadoop-common:2.7.3
    com.google.guava:guava:11.0.2
    commons-cli:commons-cli:1.2
    etc...

## Credit

Chris Nauroth from Disney who helped me figure out how to extract the group, artifact, and version from jars in the classpath: http://stackoverflow.com/a/41772308/2626491
