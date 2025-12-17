package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetResources {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取系统资源请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("=========================");
    }

    // 打印查询结果
    private void printQueryResult(Object result) {
        System.out.println("=== 数据库查询结果 ===");
        System.out.println("查询结果: " + result);
        System.out.println("===================");
    }

    // 打印返回数据
    private void printResponse(Object response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // 响应DTO
    public static class ResourcesResponse {
        private boolean success;
        private String message;
        private ResourcesData data;

        public ResourcesResponse(boolean success, String message, ResourcesData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ResourcesData getData() { return data; }
        public void setData(ResourcesData data) { this.data = data; }
    }

    public static class ResourcesData {
        private CpuInfo cpu;
        private MemoryInfo memory;
        private DiskInfo disk;
        private NetworkInfo network;
        private DatabaseInfo database;

        public ResourcesData(CpuInfo cpu, MemoryInfo memory, DiskInfo disk,
                             NetworkInfo network, DatabaseInfo database) {
            this.cpu = cpu;
            this.memory = memory;
            this.disk = disk;
            this.network = network;
            this.database = database;
        }

        public CpuInfo getCpu() { return cpu; }
        public void setCpu(CpuInfo cpu) { this.cpu = cpu; }

        public MemoryInfo getMemory() { return memory; }
        public void setMemory(MemoryInfo memory) { this.memory = memory; }

        public DiskInfo getDisk() { return disk; }
        public void setDisk(DiskInfo disk) { this.disk = disk; }

        public NetworkInfo getNetwork() { return network; }
        public void setNetwork(NetworkInfo network) { this.network = network; }

        public DatabaseInfo getDatabase() { return database; }
        public void setDatabase(DatabaseInfo database) { this.database = database; }
    }

    public static class CpuInfo {
        private double usage;
        private int cores;
        private double[] loadAverage;

        public CpuInfo(double usage, int cores, double[] loadAverage) {
            this.usage = usage;
            this.cores = cores;
            this.loadAverage = loadAverage;
        }

        public double getUsage() { return usage; }
        public void setUsage(double usage) { this.usage = usage; }

        public int getCores() { return cores; }
        public void setCores(int cores) { this.cores = cores; }

        public double[] getLoadAverage() { return loadAverage; }
        public void setLoadAverage(double[] loadAverage) { this.loadAverage = loadAverage; }
    }

    public static class MemoryInfo {
        private long total;
        private long used;
        private long free;
        private double usagePercentage;

        public MemoryInfo(long total, long used, long free, double usagePercentage) {
            this.total = total;
            this.used = used;
            this.free = free;
            this.usagePercentage = usagePercentage;
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public long getUsed() { return used; }
        public void setUsed(long used) { this.used = used; }

        public long getFree() { return free; }
        public void setFree(long free) { this.free = free; }

        public double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }
    }

    public static class DiskInfo {
        private long total;
        private long used;
        private long free;
        private double usagePercentage;

        public DiskInfo(long total, long used, long free, double usagePercentage) {
            this.total = total;
            this.used = used;
            this.free = free;
            this.usagePercentage = usagePercentage;
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public long getUsed() { return used; }
        public void setUsed(long used) { this.used = used; }

        public long getFree() { return free; }
        public void setFree(long free) { this.free = free; }

        public double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }
    }

    public static class NetworkInfo {
        private long inbound;
        private long outbound;
        private int connections;

        public NetworkInfo(long inbound, long outbound, int connections) {
            this.inbound = inbound;
            this.outbound = outbound;
            this.connections = connections;
        }

        public long getInbound() { return inbound; }
        public void setInbound(long inbound) { this.inbound = inbound; }

        public long getOutbound() { return outbound; }
        public void setOutbound(long outbound) { this.outbound = outbound; }

        public int getConnections() { return connections; }
        public void setConnections(int connections) { this.connections = connections; }
    }

    public static class DatabaseInfo {
        private int connections;
        private int maxConnections;
        private QueryTime queryTime;

        public DatabaseInfo(int connections, int maxConnections, QueryTime queryTime) {
            this.connections = connections;
            this.maxConnections = maxConnections;
            this.queryTime = queryTime;
        }

        public int getConnections() { return connections; }
        public void setConnections(int connections) { this.connections = connections; }

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public QueryTime getQueryTime() { return queryTime; }
        public void setQueryTime(QueryTime queryTime) { this.queryTime = queryTime; }
    }

    public static class QueryTime {
        private double average;
        private double max;

        public QueryTime(double average, double max) {
            this.average = average;
            this.max = max;
        }

        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }

        public double getMax() { return max; }
        public void setMax(double max) { this.max = max; }
    }

    @GetMapping("/resources")
    public ResponseEntity<ResourcesResponse> getResources() {
        // 打印接收到的请求
        printRequest("GET /api/system/resources");

        try {
            // 1. 获取系统信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            // 2. 构建CPU信息
            int availableProcessors = osBean.getAvailableProcessors();
            double systemLoadAverage = osBean.getSystemLoadAverage();
            double cpuUsage = Math.min(100.0, Math.max(0.0, (systemLoadAverage / availableProcessors) * 100));

            double[] loadAverage = new double[3];
            loadAverage[0] = systemLoadAverage;
            loadAverage[1] = systemLoadAverage * 0.8; // 模拟1分钟平均负载
            loadAverage[2] = systemLoadAverage * 0.6; // 模拟15分钟平均负载

            CpuInfo cpuInfo = new CpuInfo(
                    Math.round(cpuUsage * 10.0) / 10.0,
                    availableProcessors,
                    loadAverage
            );

            // 3. 构建内存信息
            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();

            long totalMemory = heapMemoryUsage.getCommitted() + nonHeapMemoryUsage.getCommitted();
            long usedMemory = heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed();
            long freeMemory = totalMemory - usedMemory;
            double memoryUsagePercentage = totalMemory > 0 ? ((double) usedMemory / totalMemory) * 100 : 0;

            MemoryInfo memoryInfo = new MemoryInfo(
                    totalMemory,
                    usedMemory,
                    freeMemory,
                    Math.round(memoryUsagePercentage * 10.0) / 10.0
            );

            // 4. 构建磁盘信息（模拟数据）
            long totalDisk = 107374182400L; // 100GB
            long usedDisk = 53687091200L;   // 50GB
            long freeDisk = totalDisk - usedDisk;
            double diskUsagePercentage = ((double) usedDisk / totalDisk) * 100;

            DiskInfo diskInfo = new DiskInfo(
                    totalDisk,
                    usedDisk,
                    freeDisk,
                    Math.round(diskUsagePercentage * 10.0) / 10.0
            );

            // 5. 构建网络信息（模拟数据）
            long inbound = 1024000L;  // 1MB/s
            long outbound = 512000L;   // 500KB/s
            int connections = 150;

            NetworkInfo networkInfo = new NetworkInfo(inbound, outbound, connections);

            // 6. 构建数据库信息
            int dbConnections = 25;
            int maxConnections = 100;
            double avgQueryTime = 120.0;
            double maxQueryTime = 500.0;

            // 尝试查询数据库连接数（MySQL语法）
            try {
                List<Map<String, Object>> dbStats = jdbcTemplate.queryForList(
                        "SHOW STATUS WHERE variable_name = 'Threads_connected'"
                );
                if (!dbStats.isEmpty()) {
                    String threadsConnected = dbStats.get(0).get("Value").toString();
                    dbConnections = Integer.parseInt(threadsConnected);
                }
            } catch (Exception e) {
                System.out.println("无法获取数据库连接数: " + e.getMessage());
            }

            QueryTime queryTime = new QueryTime(avgQueryTime, maxQueryTime);
            DatabaseInfo databaseInfo = new DatabaseInfo(dbConnections, maxConnections, queryTime);

            // 7. 构建响应数据
            ResourcesData resourcesData = new ResourcesData(
                    cpuInfo,
                    memoryInfo,
                    diskInfo,
                    networkInfo,
                    databaseInfo
            );

            ResourcesResponse response = new ResourcesResponse(true, "获取系统资源成功", resourcesData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取系统资源过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回错误数据
            CpuInfo errorCpu = new CpuInfo(0.0, 0, new double[]{0, 0, 0});
            MemoryInfo errorMemory = new MemoryInfo(0, 0, 0, 0.0);
            DiskInfo errorDisk = new DiskInfo(0, 0, 0, 0.0);
            NetworkInfo errorNetwork = new NetworkInfo(0, 0, 0);
            QueryTime errorQueryTime = new QueryTime(0.0, 0.0);
            DatabaseInfo errorDatabase = new DatabaseInfo(0, 0, errorQueryTime);

            ResourcesData errorData = new ResourcesData(
                    errorCpu,
                    errorMemory,
                    errorDisk,
                    errorNetwork,
                    errorDatabase
            );

            ResourcesResponse errorResponse = new ResourcesResponse(false, "获取系统资源失败: " + e.getMessage(), errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
