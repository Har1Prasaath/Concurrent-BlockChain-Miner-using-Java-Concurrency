import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiningManager {
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        AtomicBoolean found = new AtomicBoolean(false);
        BlockchainCore blockchain = new BlockchainCore();
        String data = "Block Data";

        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            int threadId = i;
            executor.submit(() -> {
                System.out.println("Thread " + threadId + " started mining.");
                Block block = Miner.mineBlock(blockchain.getLatestBlock().getHash(), data, blockchain, found);
                if (block != null) {
                    blockchain.addBlock(block);
                    System.out.println("Thread " + threadId + " successfully mined the block and earned " + 
                                      blockchain.getBlockReward() + " coins.");
                } else {
                    System.out.println("Thread " + threadId + " stopped mining without finding a valid block.");
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("Mining process completed.");
        System.out.println("Blockchain contains " + blockchain.getBlockchain().size() + " blocks.");
    }
}