package apochain;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    private final Logger logger = Logger.getLogger(getClass().getName());

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>(); // any given data (e.g. simple String message)
    public long timeStamp;
    public int nonce; //

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash(); // we need to do it after initialization of previousHash and timestamp
    }

    // calculating new hash based on blocks content
    public String calculateHash() {
        String calculatedHash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedHash;
    }

    // increase nonce value until difficulty target is reached
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); // number of 0s equals given difficulty
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        logger.info("Block mined : " + hash);
    }

    // add transaction to this block
    public boolean addTransaction(Transaction transaction) {
        // and check if transaction is valid, unless block is genesis block then ignore.
        if (transaction == null) return false;
        if ((!"0".equals(previousHash))) {
            if ((transaction.processTransaction() != true)) {
                logger.error("Transaction failed to process and has been discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        logger.info("Transaction successfully added to Block");
        return true;
    }

}
