/**
 * Created by jtbeckha on 10/20/17.
 */
class Blockchain {

    var chain: Array<Block> = arrayOf()
    var currentTransactions: Array<Transaction> = arrayOf()

    /**
     * Creates a new Block and adds it the chain.
     */
    fun newBlock() {

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
        return Block(1L, 1L, arrayOf(), 1L, "")
    }
}

fun hashBlock() {

}