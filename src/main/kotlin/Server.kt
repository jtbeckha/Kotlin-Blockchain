import blockchain.Blockchain
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.post
import org.jetbrains.ktor.routing.routing
import java.util.*

fun main(args: Array<String>) {
    // Generate a globally unique address for this node
    val nodeIdentifier = UUID.randomUUID().toString().replace("-", "")

    // Initialize the blockchain
    val blockchain = Blockchain()

    embeddedServer(Netty, 8080) {
        routing {
            get("/mine") {
                call.respondText("We'll mine a new Block", ContentType.Text.Html)
            }
            get("/chain") {
                call.respondText("We'll return the full chain")
            }
            post("/transactions/new") {
                call.respondText("We'll add a new transaction")
            }
        }
    }.start(wait = true)
}