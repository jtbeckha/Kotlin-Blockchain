import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.gson.GsonSupport
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.request.receive
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.post
import org.jetbrains.ktor.routing.routing
import java.net.URL
import java.util.*


data class FullBlockchainResponse(val chain: List<Block>, val length: Int, val message: String? = null)
data class BlockForgedResponse(val block: Block, val message: String = "New Block Forged")
data class NodeAddResponse(val totalNodes: Set<String>, val message: String = "New nodes have been added")

data class NodeAddRequest(val nodes: List<String>)

fun main(args: Array<String>) {

    // Generate a globally unique address for this node
    val nodeIdentifier = UUID.randomUUID().toString().replace("-", "")

    // Initialize the blockchain
    val blockchain = Blockchain()

    embeddedServer(Netty, 8081) {
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

                call.respond(BlockForgedResponse(block))
            }
            get("/chain") {
                call.response.status(HttpStatusCode.OK)
                call.respond(FullBlockchainResponse(blockchain.chain, blockchain.chain.size))
            }
            get("/nodes/resolve") {
                val replaced = blockchain.resolveConflicts()
                if (replaced) {
                    call.respond(FullBlockchainResponse(
                            blockchain.chain,
                            blockchain.chain.size,
                            "Our chain was replaced"
                    ))
                } else {
                    call.respond(FullBlockchainResponse(
                            blockchain.chain,
                            blockchain.chain.size,
                            "Our chain is authoritative"
                    ))
                }
            }
            post("/transactions/new") {
                val index = blockchain.newTransaction(call.receive())
                call.respondText("Transaction will be added to block " + index + "\n", ContentType.Application.Json)
            }
            post("/nodes/register") {
                val nodeAddRequest: NodeAddRequest = call.receive()
                val nodes = nodeAddRequest.nodes

                for (node in nodes) blockchain.registerNode(node)

                // Note setting the response code here doesn't actually set it due to a bug in the current version
                // of ktor, but this should work on a new version once that gets fixed.  For now any responses
                // that go through gson serialization are overridden to 200 OK
                call.response.status(HttpStatusCode.Created)
                call.respond(NodeAddResponse(blockchain.nodes
                        .map(URL::toString)
                        .toCollection(mutableSetOf())))
            }
        }
    }.start(wait = true)
}