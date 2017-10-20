/**
 * Created by jtbeckha on 10/20/17.
 */
data class Block(
        val index: Long,
        val timestamp: Long,
        val transactions: Array<Transaction>,
        val proof: Long,
        val previousHash: String
)