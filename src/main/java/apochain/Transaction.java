package apochain;

import org.apache.log4j.Logger;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

    private final Logger logger = Logger.getLogger(getClass().getName());

    public String transactionId; // contains a hash of transaction
    public PublicKey sender; // senders address (his public key)
    public PublicKey recipient; // recipients address (his public key)
    public float value; // amount of Apo coins we want to send to the recipient
    public byte[] signature; // this prevents anybody (especially miners) from spending funds in our wallet

    public ArrayList<TransactionInput> inputs;
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // count of how many transactions have been generated

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean processTransaction() {

        if (!verifySignature()) {
            logger.error("Transaction signature failed to verify");
            return false;
        }

        // gathers transaction inputs and make sure they are unspent
        for (TransactionInput i : inputs) {
            i.UTXO = ApoChain.UTXOs.get(i.transactionOutputId);
        }

        // checks transaction validity
        if (getInputsValue() < ApoChain.minimumTransaction) {
            logger.warn("Transaction inputs are too small: " + getInputsValue());
            logger.warn("Please enter the amount greater than " + ApoChain.minimumTransaction);
            return false;
        }

        // generates transaction outputs:
        float leftOver = getInputsValue() - value; // get value of inputs then the left over change
        transactionId = calulateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // send the left over 'change' back to sender

        // add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            ApoChain.UTXOs.put(o.id, o);
        }

        // remove transaction inputs from UTXO lists as spent
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; // if transaction can't be found skip it
            ApoChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; // if transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    private String calulateHash() {
        sequence++; // increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) + sequence
        );
    }
}
