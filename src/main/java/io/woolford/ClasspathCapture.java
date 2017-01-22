package io.woolford;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ClasspathCapture {

    void captureClasspath(){

        PrintWriter out = null;
        try {

            ClassLoader cl = ClassLoader.getSystemClassLoader();

            URL[] urls = ((URLClassLoader)cl).getURLs();

            out = new PrintWriter(new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream("/tmp/classpath_capture.txt")), "UTF-8"));

            for (URL url : urls){

                if (url.getFile().endsWith(".jar")){
                    JarFile jarFile = new JarFile(url.getFile());

                    for (JarEntry jarEntry : Collections.list(jarFile.entries())){
                        if (jarEntry.toString().endsWith("pom.properties")){

                            InputStream pomPropertiesInputStream = jarFile.getInputStream(jarEntry);

                            Properties properties = new Properties();
                            properties.load(pomPropertiesInputStream);

                            out.println(properties.get("groupId") + ":" + properties.get("artifactId") + ":" + properties.get("version"));

                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.flush();
                out.close();
            }
        }
    }

}