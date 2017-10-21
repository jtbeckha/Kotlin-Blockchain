package blockchain

data class Transaction(
        val sender: String,
        val recipient: String,
        val amount: Int
)
