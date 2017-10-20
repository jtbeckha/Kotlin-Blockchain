import blockchain.Block
import blockchain.Blockchain
import blockchain.Transaction
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.gson.GsonSupport
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.request.receive
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.post
import org.jetbrains.ktor.routing.routing
import java.util.*


data class FullBlockchainResponse(val chain: List<Block>, val length: Int)
data class BlockForgedResponse(val message: String = "New Block Forged", val block: Block)

fun main(args: Array<String>) {
    // Generate a globally unique address for this node
    val nodeIdentifier = UUID.randomUUID().toString().replace("-", "")

    // Initialize the blockchain
    val blockchain = Blockchain()

    embeddedServer(Netty, 8080) {
        install(GsonSupport) {
            setPrettyPrinting()
        }

        routing {
            get("/mine") {
                // Run the proof of work algorithm to get the next proof
                val proof = blockchain.proofOfWork(blockchain.lastBlock().proof)

                // We receive a reward for finding the proof
                // The sender is "0" to signify this node has mined a new coin
                blockchain.newTransaction(Transaction("0", nodeIdentifier, 1))

                // Forge new block by adding it to the chain
                val block = blockchain.newBlock(proof)

                call.respond(BlockForgedResponse(block = block))
            }
            get("/chain") {
                call.respond(FullBlockchainResponse(blockchain.chain, blockchain.chain.size))
            }
            post("/transactions/new") {
                val index = blockchain.newTransaction(call.receive())
                call.respondText("Transaction will be added to block " + index + "\n", ContentType.Application.Json)
            }
        }
    }.start(wait = true)
}