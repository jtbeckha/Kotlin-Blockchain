package main.kotlin

import java.security.MessageDigest
import java.time.Instant
import javax.xml.bind.DatatypeConverter

/**
 * Created by jtbeckha on 10/20/17.
 */
class Blockchain(
//        chain: Array<main.kotlin.Block>, currentTransactions: Array<main.kotlin.Transaction>
) {

    var chain: Array<Block> = arrayOf()
    var currentTransactions: Array<Transaction> = arrayOf()

    init {
        // Create the genesis block
        newBlock(100, "1")
    }

    /**
     * Creates a new main.kotlin.Block and adds it the chain.
     *
     * @return The new main.kotlin.Block
     */
    fun newBlock(proof: Long, previousHash: String? = null): Block {
        // FIXME Is there a better way
        var actualPreviousHash: String? = previousHash
        if (actualPreviousHash == null) actualPreviousHash = hashBlock(lastBlock())

        val newBlock = Block(
                chain.size + 1L, Instant.now().toEpochMilli(), currentTransactions, proof, actualPreviousHash
        )

        currentTransactions = arrayOf()
        chain = chain.plus(newBlock)
        return newBlock
    }

    /**
     * Creates a new transaction to go into the next mined main.kotlin.Block.
     *
     * @return Index of the block in which this transaction will be contained
     */
    fun newTransaction(sender: String, recipient: String, amount: Int): Long {
        val newTransaction = Transaction(sender, recipient, amount);
        currentTransactions = currentTransactions.plus(newTransaction);

        return lastBlock().index + 1
    }

    /**
     * Returns the last main.kotlin.Block in the chain.
     */
    fun lastBlock(): Block {
        return chain.last()
    }

    /**
     * Simple proof of work algorithm:
     *  - Fin d a number p' such that hash(pp') contains 4 leading zeroes, where p is the previous p'
     *  - p is the previous proof, and p' is the new proof
     *
     *  @return p' the new proof
     */
    fun proofOfWork(lastProof: Long): Long {
        var proof = 0L

        while (!validateProof(lastProof, proof)) {
            proof++
        }

        return proof
    }
}

/**
 * Creates a SHA-256 hash of a main.kotlin.Block.
 *
 * @return SHA-256 digest, formatted as a hex String
 */
fun hashBlock(block: Block): String {
    val algorithm = MessageDigest.getInstance("SHA-256")

    val digest = algorithm.digest(block.toString().toByteArray())
    return DatatypeConverter.printHexBinary(digest)
}

/**
 * Validates the proof of work.  i.e. does SHA256(lastProof, proof) contain 4 leading zeroes?
 */
fun validateProof(lastProof: Long, proof: Long): Boolean {
    val guess = lastProof * proof

    val algorithm = MessageDigest.getInstance("SHA-256")
//    val guessHash = algorithm.digest(guess.toByte())
    //FIXME
    return true
}