package blockchain

/**
 * Created by jtbeckha on 10/20/17.
 */
data class Transaction(
        val sender: String,
        val recipient: String,
        val amount: Int
)
