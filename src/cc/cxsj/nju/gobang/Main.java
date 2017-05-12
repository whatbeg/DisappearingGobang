package cc.cxsj.nju.gobang;

import cc.cxsj.nju.gobang.config.ServerProperties;
import cc.cxsj.nju.gobang.ui.SwingConsole;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cc.cxsj.nju.gobang.communication.ClientConnectMonitor;
import cc.cxsj.nju.gobang.info.Players;
import cc.cxsj.nju.gobang.task.CreateServiceRunnable;
import cc.cxsj.nju.gobang.ui.MainFrame;

public class Main {

    static {
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/config/log4j.properties");
    }

    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        // read config
        ServerProperties.instance();

        // start ui
        LOG.info("Startup the main ui");
        SwingConsole.run(MainFrame.instance());

        // print server propeties in ui
        ServerProperties.printServerProperties();

        // load users
        Players.loadPlayers();

        // prepare creating service
        CreateServiceRunnable.instance().start();

        // monitor connect
        ClientConnectMonitor.instance().start();

    }
}
