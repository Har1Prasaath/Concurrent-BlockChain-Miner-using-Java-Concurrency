import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiningManager {
    private static final int THREAD_POOL_SIZE = 4;
    private static final int BLOCKS_TO_MINE = 5; // Number of blocks to mine

    public static void main(String[] args) {
        BlockchainCore blockchain = new BlockchainCore();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Track mining times for each difficulty level
        long[] totalTimeByDifficulty = new long[10]; // Assuming difficulty won't exceed 10
        int[] blockCountByDifficulty = new int[10];
        
        System.out.println("Starting mining process with " + THREAD_POOL_SIZE + " threads...\n");
        
        for (int blockIndex = 1; blockIndex <= BLOCKS_TO_MINE; blockIndex++) {
            int currentDifficulty = blockchain.getDifficulty();
            long startTime = System.currentTimeMillis();
            
            System.out.println("Mining block #" + blockIndex + " with difficulty: " + currentDifficulty + 
                              " (target: " + blockchain.getTarget() + ")");
            
            mineNextBlock(blockchain, executor, "Block Data " + blockIndex);
            
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;
            
            // Record time for this difficulty level
            totalTimeByDifficulty[currentDifficulty] += timeElapsed;
            blockCountByDifficulty[currentDifficulty]++;
            
            System.out.println("\n--- Block #" + blockIndex + " mined in " + formatTime(timeElapsed) + " ---");
            System.out.println("--- Current blockchain size: " + blockchain.getBlockchain().size() + " blocks ---");
            System.out.println("--- Current difficulty: " + blockchain.getDifficulty() + " (target: " + 
                              blockchain.getTarget() + ") ---\n");
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        // Print mining statistics by difficulty
        System.out.println("\n========== MINING STATISTICS ==========");
        System.out.println("Difficulty | Blocks Mined | Avg. Time | Total Time");
        System.out.println("-----------------------------------------");
        DecimalFormat df = new DecimalFormat("#,###.##");
        
        for (int i = 0; i < totalTimeByDifficulty.length; i++) {
            if (blockCountByDifficulty[i] > 0) {
                long avgTime = totalTimeByDifficulty[i] / blockCountByDifficulty[i];
                System.out.println("    " + i + "      |      " + 
                                  blockCountByDifficulty[i] + "       | " + 
                                  formatTime(avgTime) + " | " + 
                                  formatTime(totalTimeByDifficulty[i]));
            }
        }
        
        System.out.println("\nMining process completed.");
        System.out.println("Final blockchain contains " + blockchain.getBlockchain().size() + " blocks.");
    }
    
    private static String formatTime(long timeInMs) {
        if (timeInMs < 1000) {
            return timeInMs + " ms";
        } else if (timeInMs < 60000) {
            return String.format("%.2f sec", timeInMs / 1000.0);
        } else {
            return String.format("%d min %d sec", 
                                timeInMs / 60000, 
                                (timeInMs % 60000) / 1000);
        }
    }
    
    private static void mineNextBlock(BlockchainCore blockchain, ExecutorService executor, String data) {
        AtomicBoolean found = new AtomicBoolean(false);
        int[] successThreadId = {-1}; // Array to hold the successful thread ID
        
        // Submit mining tasks to all threads
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            int threadId = i;
            executor.submit(() -> {
                System.out.println("Thread " + threadId + " started mining...");
                long threadStartTime = System.currentTimeMillis();
                Block block = Miner.mineBlock(blockchain.getLatestBlock().getHash(), data, blockchain, found);
                if (block != null) {
                    long threadEndTime = System.currentTimeMillis();
                    blockchain.addBlock(block);
                    successThreadId[0] = threadId;
                    System.out.println("Thread " + threadId + " successfully mined block after " + 
                                      formatTime(threadEndTime - threadStartTime) + 
                                      " and earned " + blockchain.getBlockReward() + " coins.");
                } else {
                    System.out.println("Thread " + threadId + " stopped mining.");
                }
            });
        }
        
        // Wait until a block is found
        while (successThreadId[0] == -1) {
            try {
                Thread.sleep(100); // Check every 100ms
                if (found.get() && blockchain.getBlockchain().size() > blockIndex(blockchain)) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Reset the found flag for all threads by setting it to true
        // This ensures all threads stop mining for the current block
        found.set(true);
        
        try {
            // Give threads time to finish their current mining attempt
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static int blockIndex(BlockchainCore blockchain) {
        return blockchain.getBlockchain().size() - 1;
    }
}