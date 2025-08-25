package com.bank;

public class JvmInfo {
    public static void main(String[] args) {
        System.out.println("=== JVM Information ===");
        System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("JVM Arguments:");
        
        // Print JVM arguments
        java.lang.management.RuntimeMXBean runtimeMxBean = java.lang.management.ManagementFactory.getRuntimeMXBean();
        java.util.List<String> arguments = runtimeMxBean.getInputArguments();
        for (String argument : arguments) {
            System.out.println("  " + argument);
        }
        
        System.out.println("========================");
    }
}