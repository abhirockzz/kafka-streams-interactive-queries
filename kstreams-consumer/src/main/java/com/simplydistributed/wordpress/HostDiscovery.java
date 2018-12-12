package com.simplydistributed.wordpress;

/**
 * to plugin some custom way of discovering your application address
 */
public interface HostDiscovery {
    public String getHost();
    
    public static final HostDiscovery localHostDiscovery = () -> "localhost";
    public static final HostDiscovery dockerDiscovery = () -> {
        System.out.println("Docker based host discovery..");
        String dockerHostName = System.getenv("HOSTNAME");
        
        System.out.println("Docker container host name - "+ dockerHostName);
        return dockerHostName;
    };

}
