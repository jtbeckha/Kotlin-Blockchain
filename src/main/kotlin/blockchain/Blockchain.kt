package blockchain

import FullBlockchainResponse
import com.google.common.primitives.Longs
import com.google.gson.Gson
import org.jetbrains.ktor.client.DefaultHttpClient
import org.jetbrains.ktor.client.readText
import org.jetbrains.ktor.http.HttpStatusCode
import java.net.URL
import java.security.MessageDigest
import java.time.Instant
import javax.xml.bind.DatatypeConverter

class Blockchain {

    val gson = Gson()

    var chain: List<Block> = listOf()
    var currentTransactions: List<Transaction> = listOf()
    var nodes: Set<URL> = setOf()

    init {
        // Create the genesis block
        newBlock(100, "1")
    }
    /**
     * Creates a new blockchain.Block and adds it the chain.
     *
     * @return The new blockchain.Block
     */
    fun newBlock(proof: Long, previousHash: String? = null): Block {
        // FIXME Is there a better way
        var actualPreviousHash: String? = previousHash
        if (actualPreviousHash == null) actualPreviousHash = hashBlock(lastBlock())

        val newBlock = Block(
                chain.size + 1L, Instant.now().toEpochMilli(), currentTransactions, proof, actualPreviousHash
        )

        currentTransactions = listOf()
        chain = chain.plus(newBlock)
        return newBlock
    }

    fun newTransaction(transaction: Transaction): Long {
        currentTransactions = currentTransactions.plus(transaction)

        return lastBlock().index + 1
    }

    /**
     * Creates a new transaction to go into the next mined blockchain.Block.
     *
     * @return Index of the block in which this transaction will be contained
     */
    fun newTransaction(sender: String, recipient: String, amount: Int): Long {
        val newTransaction = Transaction(sender, recipient, amount);
        currentTransactions = currentTransactions.plus(newTransaction);

        return lastBlock().index + 1
    }

    /**
     * Returns the last blockchain.Block in the chain.
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

    /**
     * Add a new node to the list of nodes
     */
    fun registerNode(address: String) {
        nodes = nodes.plus(URL(address))
    }

    /**
     * This is our Consensus Algorithm, it resolves conflicts by replacing our
     * chain with the longest one in the network.
     *
     * @return True if our chain was replaced, false if not
     */
    suspend fun resolveConflicts(): Boolean {
        val neighbors = nodes
        var newChain: List<Block>? = null

        // We're only looking for chains longer than ours
        var maxLength = chain.size

        for (node in neighbors) {
            val targetUrl = URL(node.toString() + "/chain")
            val response = DefaultHttpClient.request(targetUrl)
            if (response.status == HttpStatusCode.OK) {
                val otherNodeChain = gson.fromJson<FullBlockchainResponse>(
                        response.readText(), FullBlockchainResponse::class.java
                )

                // Check if the other node's chain is longer and valid
                if (otherNodeChain.length > maxLength && validateChain(otherNodeChain.chain)) {
                    maxLength = otherNodeChain.length
                    newChain = otherNodeChain.chain
                }
            }
        }

        if (newChain != null) {
            chain = newChain
            return true
        }

        return false
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

/**
 * Validates the proof of work.  i.e. does SHA256(lastProof, proof) contain 4 leading zeroes?
 */
fun validateProof(lastProof: Long, proof: Long): Boolean {
    val guess = lastProof * proof

    val algorithm = MessageDigest.getInstance("SHA-256")
    val guessDigest = algorithm.digest(Longs.toByteArray(guess))
    val guessDigestHex = DatatypeConverter.printHexBinary(guessDigest)

    return guessDigestHex.startsWith("0000")
}

/**
 * Determine if a given blockchain is valid.
 */
fun validateChain(chain: List<Block>): Boolean {
    var previousBlock = chain[0]
    var currentIndex = 1

    while (currentIndex < chain.size) {
        var block = chain[currentIndex]
        print(previousBlock)
        print(block)
        print("\n-----------\n")

        // Check that the hash of the block is correct
        if (block.previousHash != hashBlock(previousBlock)) {
            return false
        }

        // Check that the Proof of Work is correct
        if (!validateProof(previousBlock.proof, block.proof)) {
            return false
        }

        previousBlock = block
        currentIndex++
    }

    return true
}
