import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Miner {
    public static String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Block mineBlock(String previousHash, String data, BlockchainCore blockchain, AtomicBoolean found) {
        long nonce = 0;
        while (!found.get()) {
            Block block = new Block(previousHash, data, nonce);
            String hash = calculateHash(block.toString());
            if (hash.startsWith(blockchain.getTarget())) {
                if (found.compareAndSet(false, true)) {
                    block.setHash(hash);
                    return block;
                } else {
                    return null;
                }
            }
            nonce++;
        }
        return null;
    }
}