package blockchain

data class Block(
        val index: Long,
        val timestamp: Long,
        val transactions: List<Transaction>,
        val proof: Long,
        val previousHash: String
)