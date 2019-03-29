import me.dags.text.MUSpec;
import me.dags.text.MUTemplate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Plugin(id = "test", name = "Test", version = "0.0", description = ".")
public class ExamplePlugin {

    @Listener
    public void started(GameStartedServerEvent event) {
        example1();
        example2();
        example3();
    }

    public void example1() {
        MUSpec spec = MUSpec.global();
        Text text = spec.render("[Hello [world](green,underline)](blue)");
        Sponge.getServer().getBroadcastChannel().send(text);
    }

    public void example2() {
        MUSpec spec = MUSpec.global();
        Text text = spec.render("[Google: [click here](yellow,underline,https://google.com)](green)");
        Sponge.getServer().getBroadcastChannel().send(text);
    }

    public void example3() {
        MUSpec spec = MUSpec.global();
        MUTemplate template = spec.template("[Online users: {users|[{name}](green)|, }](yellow)");
        Text text = template.with("users", getOnlineUsers()).render();
        Sponge.getServer().getBroadcastChannel().send(text);
    }

    private static Collection<CommandSource> getOnlineUsers() {
        List<CommandSource> list = new ArrayList<>();
        list.add(Sponge.getServer().getConsole());
        list.addAll(Sponge.getServer().getOnlinePlayers());
        return list;
    }
}
