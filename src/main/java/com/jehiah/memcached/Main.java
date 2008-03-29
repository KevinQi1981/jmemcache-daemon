/**
 *
 * Java Memcached Server
 *
 * Created on January 2nd 2006
 * Last update January 15th 2006
 *
 * http://jehiah.com/projects/j-memcached
 *
 * Distributed under GPL
 * @author Jehiah Czebotar
 * @version 0.1
 */
package com.jehiah.memcached;

import org.apache.commons.cli.*;

import java.net.InetSocketAddress;


public class Main {

    public static void main(String[] args) throws Exception {

        // setup command line options
        Options options = new Options();
        options.addOption("h", "help", false, "print this help screen");
        options.addOption("i", "idle", true, "disconnect after idle <x> seconds");
        options.addOption("p", "port", true, "port to listen on");
        options.addOption("m", "memory", true, "max memory to use in MB");
        options.addOption("c", "ceiling", true, "ceiling memory to use in MB");
        options.addOption("l", "listen", true, "Address to listen on");
        options.addOption("V", false, "Show version number");
        options.addOption("v", false, "verbose (show commands)");

        // read command line options
        CommandLineParser parser = new PosixParser();
        CommandLine cmdline = parser.parse(options, args);

        if (cmdline.hasOption("help") || cmdline.hasOption("h")) {
            System.out.println("Memcached Version " + MemCacheDaemon.memcachedVersion);
            System.out.println("http://jehiah.com/projects/memcached\n");

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar memcached.jar", options);
            return;
        }

        if (cmdline.hasOption("V")) {
            System.out.println("Memcached Version " + MemCacheDaemon.memcachedVersion);
            return;
        }

        int port = 11211;
        if (cmdline.hasOption("p")) {
            port = Integer.parseInt(cmdline.getOptionValue("p"));
        } else if (cmdline.hasOption("port")) {
            port = Integer.parseInt(cmdline.getOptionValue("port"));
        }

        InetSocketAddress addr = new InetSocketAddress(port);
        if (cmdline.hasOption("l")) {
            addr = new InetSocketAddress(cmdline.getOptionValue("l"), port);
        } else if (cmdline.hasOption("listen")) {
            addr = new InetSocketAddress(cmdline.getOptionValue("listen"), port);
        }

        int max_size = 10000;
        if (cmdline.hasOption("s")) {
            max_size = Integer.parseInt(cmdline.getOptionValue("s"));
            System.out.println("Setting max elements to " + String.valueOf(max_size));
        } else if (cmdline.hasOption("size")) {
            max_size = Integer.parseInt(cmdline.getOptionValue("size"));
            System.out.println("Setting max elements to " + String.valueOf(max_size));
        }

        int idle = -1;
        if (cmdline.hasOption("i")) {
            idle = Integer.parseInt(cmdline.getOptionValue("i"));
        } else if (cmdline.hasOption("idle")) {
            idle = Integer.parseInt(cmdline.getOptionValue("idle"));
        }


        boolean verbose = false;
        if (cmdline.hasOption("v")) {
            verbose = true;
        }

        long ceiling;
        if (cmdline.hasOption("c")) {
            ceiling = Integer.parseInt(cmdline.getOptionValue("c")) * 1024000;
            System.out.println("Setting ceiling memory size to " + String.valueOf(ceiling) + " bytes");
        } else if (cmdline.hasOption("ceiling")) {
            ceiling = Integer.parseInt(cmdline.getOptionValue("ceiling")) * 1024000;
            System.out.println("Setting ceiling memory size to " + String.valueOf(ceiling) + " bytes");
        } else {
            ceiling = 1024000;
            System.out.println("Setting ceiling memory size to JVM limit of " + String.valueOf(ceiling) + " bytes");
        }

        long maxBytes;
        if (cmdline.hasOption("m")) {
            maxBytes = Integer.parseInt(cmdline.getOptionValue("m")) * 1024000;
            System.out.println("Setting max memory size to " + String.valueOf(maxBytes) + " bytes");
        } else if (cmdline.hasOption("memory")) {
            maxBytes = Integer.parseInt(cmdline.getOptionValue("memory")) * 1024000;
            System.out.println("Setting max memory size to " + String.valueOf(maxBytes) + " bytes");
        } else {
            maxBytes = Runtime.getRuntime().maxMemory();
            System.out.println("Setting max memory size to JVM limit of " + String.valueOf(maxBytes) + " bytes");
        }

        if (maxBytes > Runtime.getRuntime().maxMemory()) {
            System.out.println("ERROR : JVM heap size is not big enough. use '-Xmx" + String.valueOf(maxBytes / 1024000) + "m' java argument before the '-jar' option.");
            return;
        }

        // create daemon and start it
        MemCacheDaemon daemon = new MemCacheDaemon();
        LRUCacheDelegate cacheDelegate = new LRUCacheDelegate(max_size, max_size, 1024000);
        daemon.setCacheDelegate(cacheDelegate);
        daemon.start();
    }

}
