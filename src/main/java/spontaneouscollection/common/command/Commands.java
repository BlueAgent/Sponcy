package spontaneouscollection.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import spontaneouscollection.SpontaneousCollection;
import spontaneouscollection.common.helper.LangHelper;
import spontaneouscollection.common.helper.ShopHelper;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@Commands.CommandHolder("commands.")
public class Commands {
    public static final String COMMAND_EXCEPTION = "commands.exception";
    public static final LangHelper lang = new LangHelper("commands.");

    @Command
    public static void sc_test(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.addChatMessage(lang.getTextComponent("sc_test.message", String.join(" ", args)));
    }

    @Command
    public static void sc_error(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        throw new CommandException(lang.getKey("sc_error.error"));
    }

    @Command
    public static void sc_exception(MinecraftServer server, ICommandSender sender, String[] args) {
        throw new RuntimeException("Oh noes, something broke.");
    }

    //TODO: Make it put into into the network
    @Command
    public static void sc_insert(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, SQLException {
        if (!(sender instanceof EntityPlayer)) throw new CommandException(lang.getKey("exception.not_a_player"));
        EntityPlayer player = (EntityPlayer) sender;
        ItemStack stack = player.getHeldItemMainhand();
        ShopHelper shops = SpontaneousCollection.proxy.shops;
        Connection conn = shops.getConnection();
    }

    //TODO: Remove Debug or limit to OPs
    @Command
    public static void sc_sql(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, SQLException {
        ShopHelper shops = SpontaneousCollection.proxy.shops;
        Connection conn = shops.getConnection();
        conn.setAutoCommit(false);
        Statement s = conn.createStatement();
        String sql = String.join(" ", args);
        boolean hasResults = s.execute(sql);
        if (hasResults) {
            ResultSet results;
            do {
                results = s.getResultSet();
                ((EntityPlayer) sender).addChatComponentMessage(new TextComponentString(results.toString()));
            }
            while (s.getMoreResults());
        }
        s.close();
        conn.commit();
        sender.addChatMessage(lang.getTextComponent("sc_sql.success", s.getUpdateCount()));
    }

    public static List<ICommand> getCommands(Class c) {
        List<ICommand> commands = new LinkedList<>();
        for (Method m : c.getDeclaredMethods()) {
            if ((m.getModifiers() & Modifier.STATIC) == 0) continue;
            if (m.getAnnotation(Command.class) == null) continue;
            m.setAccessible(true);
            commands.add(new CommandInstance(m));
        }
        return commands;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Command {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Usage {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface CommandHolder {
        String value() default "";
    }

    public static class CommandInstance extends CommandBase {
        protected final String prefix;
        protected final String name;
        protected final String usage;
        protected final Method method;

        public CommandInstance(Method method) {
            String prefix = "";
            String name = method.getName();
            String usage = method.getName();

            Annotation a = method.getDeclaringClass().getAnnotation(CommandHolder.class);
            if (a != null)
                prefix = ((CommandHolder) a).value();

            a = method.getAnnotation(Command.class);
            if (a != null && ((Command) a).value().trim().length() != 0)
                name = ((Command) a).value();

            a = method.getAnnotation(Usage.class);
            if (a != null)
                usage = ((Usage) a).value();

            this.prefix = prefix;
            this.name = name;
            this.usage = usage;
            this.method = method;
        }

        @Override
        public String getCommandName() {
            return name;
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return prefix + usage;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            try {
                method.invoke(null, server, sender, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException ite) {
                Throwable e = ite.getCause();
                if (e instanceof CommandException)
                    throw (CommandException) e;
                else
                    throw new CommandException(COMMAND_EXCEPTION, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
