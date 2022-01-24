package com.example.prajnasocketmodeapp

import android.app.Application
import android.system.Os.setenv
import com.slack.api.Slack
import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.model.event.ReactionAddedEvent
import com.slack.api.socket_mode.request.EventsApiEnvelope
import com.slack.api.socket_mode.response.AckResponse
import com.slack.api.socket_mode.response.SocketModeResponse

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        setenv(
            "SLACK_APP_TOKEN",
            "<xapp-token>",
            true
        )
        setenv(
            "SLACK_BOT_TOKEN",
            "<xoxb-token>",
            true
        )

        // setup socket mode on a new thread
        Thread {
            setUpSocketMode()
        }.start()
    }

    private fun setUpSocketMode() {
        val app = App()

        // Responds to slash command
        app.command("/prajna-command") { req, ctx -> ctx.ack(":wave: Hello!") }

        // Responds to events api `reaction_added` event.
        app.event(ReactionAddedEvent::class.java) { payload, ctx ->
            val event: ReactionAddedEvent = payload.event
            if (event.reaction.equals("white_check_mark")) {
                val message: ChatPostMessageResponse = ctx.client().chatPostMessage { r ->
                    r
                        .channel(event.item.channel)
                        .threadTs(event.item.ts)
                        .text(
                            "<@" + event.user
                                .toString() + "> Thank you! We greatly appreciate your efforts :two_hearts:"
                        )
                }
                if (!message.isOk) {
                    ctx.logger.error("chat.postMessage failed: {}", message.error)
                }
            }
            ctx.ack()
        }
        SocketModeApp(app).start()
    }


    fun setUpSocketMode_2() {
        val appLevelToken =
            "<xapp-token>"

        val client = Slack.getInstance().socketMode(appLevelToken)

        // SocketModeClient has #close() method

        // Add a listener function to handle all raw WebSocket text messages
        // You can handle not only envelopes but also any others such as "hello" messages.
        client.addWebSocketMessageListener { message: String? ->
            // Listen to events here
        }
        client.addWebSocketErrorListener { reason: Throwable? -> }

        // Add a listener function that handles only type: events envelopes
        client.addEventsApiEnvelopeListener { envelope: EventsApiEnvelope ->
            // TODO: Do something with an Events API payload
            // Acknowledge the request (within 3 seconds)
            val ack: SocketModeResponse =
                AckResponse.builder().envelopeId(envelope.envelopeId).build()
            client.sendSocketModeResponse(ack)

            client.connect() // Start receiving messages from the Socket Mode server
        }
    }
}