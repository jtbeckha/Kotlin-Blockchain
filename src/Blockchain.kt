import java.security.MessageDigest
import java.time.Instant
import java.util.*
import javax.xml.bind.DatatypeConverter

/**
 * Created by jtbeckha on 10/20/17.
 */
class Blockchain(
//        chain: Array<Block>, currentTransactions: Array<Transaction>
) {

    var chain: Array<Block> = arrayOf()
    var currentTransactions: Array<Transaction> = arrayOf()

    init {
        // Create the genesis block
        newBlock(100, "1")
    }

    /**
     * Creates a new Block and adds it the chain.
     *
     * @return The new Block
     */
    fun newBlock(proof: Long, previousHash: String? = null): Block {
        // FIXME Is there a better way
        var actualPreviousHash: String? = previousHash
        if (actualPreviousHash == null) actualPreviousHash = hashBlock()

        val newBlock = Block(
                chain.size + 1L, Instant.now().toEpochMilli(), currentTransactions, proof, actualPreviousHash
        )

        currentTransactions = arrayOf()
        chain = chain.plus(newBlock)
        return newBlock
    }

    /**
     * Creates a new transaction to go into the next mined Block.
     *
     * @return Index of the block in which this transaction will be contained
     */
    fun newTransaction(sender: String, recipient: String, amount: Int): Long {
        val newTransaction = Transaction(sender, recipient, amount);
        currentTransactions = currentTransactions.plus(newTransaction);

        return lastBlock().index + 1
    }

    /**
     * Returns the last Block in the chain.
     */
    fun lastBlock(): Block {
        return chain.last()
    }
}

/**
 * Creates a SHA-256 hash of a Block.
 *
 * @return SHA-256 digest, formatted as a hex String
 */
fun hashBlock(block: Block): String {
    val algorithm = MessageDigest.getInstance("SHA-256")

    val digest = algorithm.digest(block.toString().toByteArray())
    return DatatypeConverter.printHexBinary(digest)
}