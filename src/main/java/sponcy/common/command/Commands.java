package sponcy.common.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sponcy.Sponcy;
import sponcy.common.SponcyConfig;
import sponcy.common.helper.*;
import sponcy.common.sql.ShopOwner;

import java.io.IOException;
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
import java.util.UUID;

@Commands.CommandHolder("commands.")
public class Commands {
    public static final LangHelper lang = new LangHelper("commands.");
    public static final String COMMAND_EXCEPTION = lang.getKey("exception");
    public static final String NOT_A_PLAYER = lang.getKey("exception.not_a_player");
    public static final String NOT_OP = lang.getKey("exception.not_op");

    @Command
    public static void sc_test(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(lang.getTextComponent("sc_test.message", String.join(" ", args)));
        sender.sendMessage(new TextComponentString("isFake: " + PlayerHelper.isFake(sender)));
    }

    @Command
    public static void sc_error(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        throw new CommandException(lang.getKey("sc_error.error"));
    }

    @Command
    public static void sc_exception(MinecraftServer server, ICommandSender sender, String[] args) {
        throw new RuntimeException("Oh noes, something broke.");
    }

    @Command
    public static void sc_reload(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(FMLCommonHandler.instance().getSide().isClient()) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            mc.addScheduledTask(() -> {
                try {
                    SimpleReloadableResourceManager manager = (SimpleReloadableResourceManager) mc.getResourceManager();
                    manager.reloadResourcePack(FMLClientHandler.instance().getResourcePackFor(Sponcy.MOD_ID));
                } catch (ClassCastException cce) {
                    Sponcy.log.error("Expected a SimpleReloadableResourceManager", cce);
                }
            });
        }
    }

    //TODO: Make it put into into the network
    @Command
    public static void sc_insert(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, SQLException {
        if (!(sender instanceof EntityPlayer)) throw new CommandException(lang.getKey("exception.not_a_player"));
        EntityPlayer player = (EntityPlayer) sender;
        final ItemStack stack = player.getHeldItemMainhand();

        if (stack.isEmpty()) {
            throw new CommandException(lang.getKey("sc_insert.missing"));
        }
        //TODO: Remove Debug or limit to OPs
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);

        ShopHelper shops = Sponcy.proxy.shops;
        Connection conn = shops.getConnection();

    }

    @Command
    public static void sc_sql(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, SQLException {
        if (SponcyConfig.Shops.disable_sc_sql) new CommandException(lang.getKey("sc_sql.disabled"));
        if (!isOp(sender)) new CommandException(NOT_OP);

        final ShopHelper shops = Sponcy.proxy.shops;
        final String sql = String.join(" ", args);

        shops.run(() -> {
            long duration = System.nanoTime();
            final LinkedList<ITextComponent> components = new LinkedList<>();
            try {
                Connection conn = shops.getConnection();
                SQLiteHelper.rollbackAndThrowWithCommit(conn, () -> {
                    Statement s = conn.createStatement();
                    boolean hasResults = s.execute(sql);
                    if (hasResults) {
                        ResultSet results = s.getResultSet();
                        while (results.next()) {
                            components.add(new TextComponentString(StringHelper.stringify(results)));
                        }
                    }
                    s.close();
                    int updates = s.getUpdateCount();
                    if (updates < 0)
                        components.add(lang.getTextComponent("sc_sql.success.no_changes"));
                    else
                        components.add(lang.getTextComponent("sc_sql.success", updates));
                });
            } catch (SQLException e) {
                components.add(LangHelper.NO_PREFIX.getTextComponent(e));
            }
            duration = System.nanoTime() - duration;
            //Convert nanoseconds to milliseconds
            components.addFirst(new TextComponentString((duration / 1000000.0) + "ms"));
            //Add use of this command to the server log
            server.logWarning(sender.getName()
                    + " used sc_sql. Query: " + sql
                    + " Results: " + String.join("\n", components.stream().map((tc) -> tc.getFormattedText()).toArray((len) -> new String[len])));
            SyncHelper.addScheduledTaskOrRunNow(false, () -> {
                components.forEach(sender::sendMessage);
            });
        });
    }

    @Command
    public static void sc_owner(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException, SQLException {
        final ShopHelper shops = Sponcy.proxy.shops;
        final SQLiteHelper.ISQLFunction<ShopOwner> method_temp;
        if (args.length == 0) {
            if (!(sender instanceof EntityPlayer))
                throw new CommandException(NOT_A_PLAYER);
            method_temp = () -> shops.getOwner((EntityPlayer) sender);
        } else if (args.length == 1) {
            final String input = args[0];
            method_temp = () -> {
                try {
                    int id = Integer.parseInt(input);
                    return shops.getOwner(id);
                } catch (NumberFormatException e) {
                }
                try {
                    UUID id = UUID.fromString(input);
                    return shops.getOwner(id);
                } catch (IllegalArgumentException e) {
                }
                return shops.getOwner(input);
            };
        } else {
            throw new WrongUsageException(lang.getKey("sc_owner.usage"));
        }

        final SQLiteHelper.ISQLFunction<ShopOwner> method = method_temp;
        shops.run(() -> {
            final List<ITextComponent> components = new LinkedList<>();
            try {
                ShopOwner owner = method.run();
                components.add(new TextComponentString(owner.toString()));
            } catch (SQLException e) {
                components.add(LangHelper.NO_PREFIX.getTextComponent(e));
            }
            SyncHelper.addScheduledTaskOrRunNow(false, () -> {
                components.forEach(sender::sendMessage);
            });
        });
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

    public static boolean isOp(ICommandSender sender) {
        return sender.canUseCommand(2, "");
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
        public String getName() {
            return name;
        }

        @Override
        public String getUsage(ICommandSender sender) {
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
