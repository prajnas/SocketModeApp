package com.example.prajnasocketmodeapp

import android.os.Bundle
import android.system.Os
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.prajnasocketmodeapp.databinding.ActivityMainBinding
import com.slack.api.Slack
import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.model.event.ReactionAddedEvent
import com.slack.api.socket_mode.request.EventsApiEnvelope
import com.slack.api.socket_mode.response.AckResponse
import com.slack.api.socket_mode.response.SocketModeResponse
import com.slack.api.socket_mode.SocketModeClient
import com.slack.api.socket_mode.listener.EnvelopeListener
import com.slack.api.socket_mode.listener.WebSocketErrorListener
import com.slack.api.socket_mode.listener.WebSocketMessageListener
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.reflect.Field
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Os.setenv("SLACK_APP_TOKEN", "xapp-1-A030EB1C332-3014377336080-631d327c1e5035a3b44aebb89bc9a85d74ebc884ce3d5e91ba2163cb455169e9", true)
        Os.setenv("SLACK_BOT_TOKEN", "xoxb-1120683590070-2990679298595-LvEE0hrdGF3caLy4JnOqEsXy", true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        Log.d("prajna", "env value= " + System.getenv("SLACK_APP_TOKEN"))

        binding.fab.setOnClickListener { view ->
            val thread: Thread = object : Thread() {
                override fun run() {
                    try {
                        client()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }

            thread.start()

            //client()
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    fun client() {
        val app = App()
        app.command("/prajna-command") { req, ctx -> ctx.ack(":wave: Hello!") }

        app.event(ReactionAddedEvent::class.java) { payload, ctx ->
            val event: ReactionAddedEvent = payload.getEvent()
            if (event.getReaction().equals("white_check_mark")) {
                val message: ChatPostMessageResponse = ctx.client().chatPostMessage { r ->
                    r
                        .channel(event.getItem().getChannel())
                        .threadTs(event.getItem().getTs())
                        .text(
                            "<@" + event.getUser()
                                .toString() + "> Thank you! We greatly appreciate your efforts :two_hearts:"
                        )
                }
                if (!message.isOk()) {
                    ctx.logger.error("chat.postMessage failed: {}", message.getError())
                }
            }
            ctx.ack()
        }
        SocketModeApp(app).start()
    }


    fun worksclient() {
        val appLevelToken = "xapp-1-A030EB1C332-3014377336080-631d327c1e5035a3b44aebb89bc9a85d74ebc884ce3d5e91ba2163cb455169e9"

        val client = Slack.getInstance().socketMode(appLevelToken)

            // SocketModeClient has #close() method

            // Add a listener function to handle all raw WebSocket text messages
            // You can handle not only envelopes but also any others such as "hello" messages.
            client.addWebSocketMessageListener { message: String? ->
                Log.d("prajna", "hhh")
            }
            client.addWebSocketErrorListener { reason: Throwable? -> }

            // Add a listener function that handles only type: events envelopes
            client.addEventsApiEnvelopeListener { envelope: EventsApiEnvelope ->
                // TODO: Do something with an Events API payload
                Log.d("prajna", "QQQ")
                // Acknowledge the request (within 3 seconds)
                val ack: SocketModeResponse =
                    AckResponse.builder().envelopeId(envelope.envelopeId).build()
                client.sendSocketModeResponse(ack)

            client.connect() // Start receiving messages from the Socket Mode server
        }
    }


    @Throws(Exception::class)
    fun set(newenv: Map<String, String>?) {
        val classes: Array<Class<*>> = Collections::class.java.getDeclaredClasses()
        val env = System.getenv()
        for (cl in classes) {
            if ("java.util.Collections\$UnmodifiableMap" == cl.name) {
                val field: Field = cl.getDeclaredField("m")
                field.setAccessible(true)
                val obj: Any = field.get(env)
                val map = obj as MutableMap<String, String>
                map.clear()
                map.putAll(newenv!!)
            }
        }
    }
}