package apochain;

import org.apache.log4j.Logger;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

//import java.util.Base64;
//import com.google.gson.GsonBuilder;


public class ApoChain {

    private static final Logger logger = Logger.getLogger(ApoChain.class);

    private static ArrayList<Block> blockchain = new ArrayList<>();
    static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    private static int difficulty = 5;
    static float minimumTransaction = 0.1f;
    private static Wallet walletA;
    private static Wallet walletB;
    private static Transaction genesisTransaction;

    public static void main(String[] args) {

        // add our blocks to the blockchain ArrayList:
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // setup Bouncey castle as a Security Provider tool

        // create wallets:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // create genesis transaction, which sends 100 ApoCoin to wallet A:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);     // manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; // manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); // manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // its important to store our first transaction in the UTXOs list

        logger.info("Creating genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //testing
        Block block1 = new Block(genesis.hash);
        logger.info("WalletA's balance is: " + walletA.getBalance());
        logger.info("WalletA is trying to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        logger.info("WalletA's balance is: " + walletA.getBalance());
        logger.info("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        logger.info("WalletA trying to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        logger.info("WalletA's balance is: " + walletA.getBalance());
        logger.info("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        logger.info("WalletB is trying to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
        logger.info("WalletA's balance is: " + walletA.getBalance());
        logger.info("WalletB's balance is: " + walletB.getBalance());

        isChainValid();

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>(); // a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        // loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // compare registered hash and calculated hash:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                logger.error("Current hashes not equal");
                return false;
            }
            // compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                logger.error("Previous hashes not equal");
                return false;
            }
            // check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                logger.error("This block hasn't been mined");
                return false;
            }

            // loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifySignature()) {
                    logger.error("Signature on transaction(" + t + ") is invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    logger.error("Inputs are note equal to outputs on transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        logger.error("Referenced input on transaction(" + t + ") is missing");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        logger.error("Referenced input transaction(" + t + ") value is invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    logger.error("Transaction(" + t + ") output recipient is not who it should be");
                    return false;
                }
                if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    logger.error("Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        logger.info("Blockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}

/*
 * public static void main(String[] args) {	
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		
		//walletA = new Wallet();
		//walletB = new Wallet();
		
		//System.out.println("Private and public keys:");
		//System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		//System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		createGenesis();
		
		//Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5);
		//transaction.signature = transaction.generateSignature(walletA.privateKey);
		
		//System.out.println("Is signature verified:");
		//System.out.println(transaction.verifiySignature());
		
	}
 */

//System.out.println("Trying to Mine block 1... ");
//addBlock(new Block("Hi im the first block", "0"));
