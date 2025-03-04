import java.util.ArrayList;
import java.util.List;

public class BlockchainCore {
    private static final int INITIAL_DIFFICULTY = 2; // Start with difficulty 2
    private static final int BLOCK_REWARD = 50;
    private static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 1; // Adjust after every block

    private List<Block> blockchain;
    private int difficulty;

    public BlockchainCore() {
        this.blockchain = new ArrayList<>();
        this.difficulty = INITIAL_DIFFICULTY;
        createGenesisBlock();
    }

    private void createGenesisBlock() {
        Block genesisBlock = new Block("0", "Genesis Block", 0);
        String hash = calculateHash(genesisBlock);
        genesisBlock.setHash(hash);
        blockchain.add(genesisBlock);
        System.out.println("Blockchain initialized with genesis block: " + hash);
    }

    public synchronized void addBlock(Block block) {
        if (isValidBlock(block)) {
            blockchain.add(block);
            System.out.println("Block added to blockchain with hash: " + block.getHash());
            adjustDifficulty();
        } else {
            System.out.println("Invalid block rejected: " + block.getHash());
        }
    }

    public boolean isValidBlock(Block block) {
        Block latestBlock = getLatestBlock();
        boolean isPreviousHashValid = block.getPreviousHash().equals(latestBlock.getHash());
        boolean isHashValid = block.getHash().startsWith(getTarget());
        boolean isHashCalculationCorrect = block.getHash().equals(calculateHash(block));
        
        return isPreviousHashValid && isHashValid && isHashCalculationCorrect;
    }

    private synchronized void adjustDifficulty() {
        if (blockchain.size() % DIFFICULTY_ADJUSTMENT_INTERVAL == 0 && blockchain.size() > 0) {
            difficulty += 1;
            System.out.println("Difficulty adjusted to: " + difficulty + " (target: " + getTarget() + ")");
        }
    }

    public synchronized Block getLatestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    public synchronized String getTarget() {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String calculateHash(Block block) {
        return Miner.calculateHash(block.toString());
    }

    public synchronized List<Block> getBlockchain() {
        return new ArrayList<>(blockchain);
    }

    public int getBlockReward() {
        return BLOCK_REWARD;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
}