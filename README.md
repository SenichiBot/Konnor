# 🚀 `Konnor`
An efficient-cool Discord Bot with Slash Commands that uses DiscordInteraktions and Kord!

> Can I use this on my project?
* Of course ^^. Also, be original! Don't use the same name, it's not that hard to choose a cool name.

> How can I contribute to the project?
* First, you'll need to clone the repository and have JDK 17 & IntelliJ IDEA installed.
* For the newbies (like [this](https://github.com/hechfx) person) there's a simple example of a command.
```kotlin
object PingCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("ping", "ping command description") {
        executor = PingCommandExecutor
    }
}

class PingCommandExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(PingCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions() {
            val ephemeral = boolean("ephemeral", "Send the message as ephemeral")
                .register()
        }
        
        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
       if (args[options.ephemeral]) {
           context.sendEphemeralMessage {
               content = "pong!"
           }
       } else {
           context.sendMessage {
               content = "pong!"
           }
       }
    }
}
```