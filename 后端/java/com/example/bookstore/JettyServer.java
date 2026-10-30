package com.example.bookstore;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import java.io.File;

/**
 * Jetty 内嵌启动，webapp 目录通过启动参数或相对路径自适应：
 *   方式 1: java -cp ... com.example.bookstore.JettyServer [webappDir] [classesDir] [port]
 *   方式 2: 默认自动探测（当前工作目录下找 ./webapp 或 ./src/main/webapp）
 */
public class JettyServer {

    public static void main(String[] args) throws Exception {
        String[] defaultPaths = detectDefaultPaths();
        String webappDir = args.length > 0 && !args[0].isEmpty() ? args[0] : defaultPaths[0];
        String classesDir = args.length > 1 && !args[1].isEmpty() ? args[1] : defaultPaths[1];
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 8080;

        Server server = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");

        File webRoot = new File(webappDir).getCanonicalFile();
        webapp.setResourceBase(webRoot.getAbsolutePath());
        File webXml = new File(webRoot, "WEB-INF/web.xml");
        if (webXml.exists()) webapp.setDescriptor(webXml.getAbsolutePath());

        webapp.setConfigurations(new Configuration[] {
            new AnnotationConfiguration(),
            new WebInfConfiguration(),
            new WebXmlConfiguration(),
            new MetaInfConfiguration(),
            new FragmentConfiguration(),
            new JettyWebXmlConfiguration()
        });

        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
            ".*/[^/]*\\.jar$");
        webapp.setParentLoaderPriority(true);

        // 额外 classpath（编译产物目录）
        if (classesDir != null && new File(classesDir).exists()) {
            webapp.setExtraClasspath(new File(classesDir).getCanonicalPath());
        }

        server.setHandler(webapp);

        System.out.println("Starting Jetty server on http://0.0.0.0:" + port);
        System.out.println("  webapp:  " + webRoot.getAbsolutePath());
        System.out.println("  classes: " + (classesDir != null ? new File(classesDir).getCanonicalPath() : "(WEB-INF/classes)"));

        server.start();
        server.join();
    }

    /** 自动探测：优先 ./webapp（deploy 包结构），回退 ./src/main/webapp（本地开发结构） */
    private static String[] detectDefaultPaths() {
        String cwd = System.getProperty("user.dir");
        File candidate;

        // 1. deploy 包结构
        candidate = new File(cwd, "webapp");
        if (candidate.exists() && new File(candidate, "WEB-INF").exists()) {
            String classes = new File(cwd, "classes").exists() ? "./classes" : null;
            return new String[] { candidate.getAbsolutePath(), classes };
        }

        // 2. 本地开发结构
        candidate = new File(cwd, "src/main/webapp");
        if (candidate.exists()) {
            return new String[] { candidate.getAbsolutePath(), "./target/classes" };
        }

        // 3. 工作目录直接就是 webapp
        if (new File(cwd, "WEB-INF").exists()) {
            return new String[] { cwd, null };
        }

        // 兜底：硬编码（原来的行为）
        return new String[] { "./src/main/webapp", "./target/classes" };
    }
}