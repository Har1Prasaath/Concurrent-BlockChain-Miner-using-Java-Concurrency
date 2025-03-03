public class Block {

    private String previousHash;
    private String data;
    private long nonce;
    private String hash;

    public Block(String previousHash, String data, long nonce) {
        this.previousHash = previousHash;
        this.data = data;
        this.nonce = nonce;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return data;
    }

    public long getNonce() {
        return nonce;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return previousHash + data + nonce;
    }
}