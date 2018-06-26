package apochain;

import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey recipient; // new owner of Apo coins
    public float value; //the amount of Apo coins they own
    public String parentTransactionId; // the id of the transaction this output was created in

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
    }

    // checks if Apo coins belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }

}
