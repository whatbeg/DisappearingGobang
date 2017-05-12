package cc.cxsj.nju.gobang.task;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import cc.cxsj.nju.gobang.Main;
import cc.cxsj.nju.gobang.config.ServerProperties;
import cc.cxsj.nju.gobang.info.Player;
import cc.cxsj.nju.gobang.info.Players;
import cc.cxsj.nju.gobang.ui.MainFrame;
import org.apache.log4j.Logger;

public class CreateServiceRunnable extends Thread {

	private static final Logger LOG = Logger.getLogger(CreateServiceRunnable.class);
	private static final Integer MODE = Integer.valueOf(ServerProperties.instance().getProperty("server.mode"));
	private static boolean isFilterOut;

	static CreateServiceRunnable instance = new CreateServiceRunnable();

	private HashSet<String> isContested = new HashSet<String>();

	private BlockingQueue<Socket> sockets;
	private ArrayList<Player> matchPlayerList;
	private HashSet<Player> matchPlayerSet;
	private ExecutorService executor;

	public static CreateServiceRunnable instance() {
		return instance;
	}

	public static void updateService() {
		instance.clear();
		LOG.info("Update CreateServiceRunnable!");
		MainFrame.instance().log("Update CreateServiceRunnable!");
		instance = new CreateServiceRunnable();
		instance.start();
	}

	public synchronized void addSocket(Socket socket) {
		if (sockets == null) {
			LOG.info("This round of contest is over!");
			MainFrame.instance().log("This round of contest is over!");
			try {
				socket.close();
			} catch (IOException e) {
				LOG.error(e);
			}
			return;
		}
		try {
			this.sockets.put(socket);
		} catch (InterruptedException e) {
			LOG.error("InterruptedException when add socket");
			System.exit(0);
		}
	}

	private CreateServiceRunnable() {
		int isFilterOut = Integer.valueOf(ServerProperties.instance().getProperty("filter.out"));
		switch (isFilterOut) {
			case 0:
				CreateServiceRunnable.isFilterOut = false;
				break;
			case 1:
				CreateServiceRunnable.isFilterOut = true;
				break;
			default:
				LOG.error("Server filter.out property is invalid");
				System.exit(0);
				break;
		}
		this.sockets = new LinkedBlockingQueue<Socket>();
		this.matchPlayerList = new ArrayList<Player>();
		this.matchPlayerSet = new HashSet<Player>();
		this.executor = Executors.newFixedThreadPool(Players.getPlayersNum() / 2 + 1);
		LOG.info("CreateServiceRunnable initialization complete!");
		MainFrame.instance().log("CreateServiceRunnable initialization complete!");
	}

	private void clear() {
		interrupt();
		executor.shutdownNow();
	}

	@Override
	public void run() {

		LOG.info("CreateServiceRunnable is running...");
		MainFrame.instance().log("CreateServiceRunnable is running...");

		byte[] buffer = new byte[16];

		// start different server mode according to server.mode value
		switch (MODE) {
		case 0: {
			// contest mode
			LOG.info("Start contest mode!");
			MainFrame.instance().log("Start contest mode!");

			Socket socket = null;
			Player user = null;
			BufferedInputStream bfin = null;
			try {
				// blocking to get two socket
				while (!Thread.currentThread().isInterrupted() && matchPlayerSet.size() != Players.getPlayersNum()) {

					LOG.info("The number of registered players: " + matchPlayerSet.size());
					MainFrame.instance().log("The number of registered players: " + matchPlayerSet.size());

					while (!Thread.currentThread().isInterrupted()) {
						try {
							// block
							socket = this.sockets.take();
						} catch (InterruptedException e) {
						    e.printStackTrace();
							LOG.error(e);
							break;
						}

						try {
							socket.setSoTimeout(
									Integer.valueOf(ServerProperties.instance().getProperty("step.timeout")));
						} catch (SocketException e) {
						    e.printStackTrace();
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						try {
							bfin = new BufferedInputStream(socket.getInputStream());
						} catch (IOException e) {
                            e.printStackTrace();
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
							    e.printStackTrace();
								LOG.error(e);
							}
							continue;
						}

						try {
							// block
							bfin.read(buffer);
						} catch (IOException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						String msg = new String(buffer);
						Arrays.fill(buffer, (byte) 0);
						if (msg.charAt(0) == 'A' && msg.length() == 16) {
							String id = msg.substring(1, 10);
							if (isFilterOut && isContested.contains(id)) {
								LOG.error("The " + id + " has participated in the contest");
								MainFrame.instance().log("The " + id + " has participated in the contest");
								try {
									bfin.close();
									socket.close();
									socket = null;
									bfin = null;
								} catch (IOException e) {
									LOG.error(e);
								}
								continue;
							}
							String password = msg.substring(10, 16);
							LOG.info("Accepted " + id);
							MainFrame.instance().log("Accepted " + id);
							if (Players.isContainedPlayer(id) && !matchPlayerSet.contains(Players.getPlayer(id))) {
								user = Players.getPlayer(id);
								if (user.getPassword().equals(password)) {
									try {
										user.initial(socket, bfin);
									} catch (IOException e) {
										LOG.error(e);
										user.clear();
										socket = null;
										bfin = null;
										continue;
									}
									isContested.add(id);
									matchPlayerSet.add(user);
									matchPlayerList.add(user);
									LOG.info("Welcome " + id);
									MainFrame.instance().log("Welcome " + id);
									break;
								} else {
									try {
										bfin.close();
										socket.close();
										socket = null;
										bfin = null;
									} catch (IOException e) {
										LOG.error(e);
									}
									LOG.info("Password is wrong: " + password);
									MainFrame.instance().log("Password is wrong: " + password);
								}
							} else {
								try {
									bfin.close();
									socket.close();
									socket = null;
									bfin = null;
								} catch (IOException e) {
									LOG.error(e);
								}
								LOG.info("No authorized " + id);
								MainFrame.instance().log("No authorized " + id);
							}
						} else {
							try {
								bfin.close();
								socket.close();
								socket = null;
								bfin = null;
							} catch (IOException e) {
								LOG.error(e);
							}
							LOG.info("Authorize msg format error");
							MainFrame.instance().log("Authorize msg format error");
						}
					}

				}

				synchronized (this) {
					while (!sockets.isEmpty()) {
						try {
							sockets.take().close();
						} catch (IOException e) {
							LOG.error(e);
						} catch (InterruptedException e) {
							LOG.error(e);
						}
					}
					this.sockets = null;
				}

				if (!Thread.currentThread().isInterrupted()) {
					LOG.info("The number of registered players: " + matchPlayerSet.size());
					for (int i = 0; i < matchPlayerList.size(); i++)
                        MainFrame.instance().log(matchPlayerList.get(i).toString());
					MainFrame.instance().log("The number of registered players: " + matchPlayerList.size());
					Collections.shuffle(matchPlayerList, new Random(System.currentTimeMillis()));
					if (matchPlayerList.size() % 2 == 0) {
						LOG.info("No passed player in contest");
						MainFrame.instance().log("No passed player in contest");
						LOG.info("Contest start!");
						MainFrame.instance().log("Contest start!");
						for (int i = 0; i < matchPlayerList.size(); i += 2) {
							ContestServiceRunnable csr = new ContestServiceRunnable(matchPlayerList.get(i),
									matchPlayerList.get(i + 1));
							this.executor.execute(csr);
						}
					} else {
						Player passedPlayer = matchPlayerList.get(matchPlayerList.size() - 1);
						passedPlayer.clear();
						LOG.info("The passed player in contest: " + passedPlayer.getId());
						MainFrame.instance().log("The passed player in contest: " + passedPlayer.getId());
						LOG.info("Contest start!");
						MainFrame.instance().log("Contest start!");
						for (int i = 0; i < matchPlayerList.size() - 1; i += 2) {
							ContestServiceRunnable csr = new ContestServiceRunnable(matchPlayerList.get(i),
									matchPlayerList.get(i + 1));
							this.executor.execute(csr);
						}
					}
				}
			} finally {
//				LOG.info("All peer contest done!");
//				MainFrame.instance().log("All peer contest done!");
//				System.out.println("All peer contest done!");
			}
			break;
		}
		case 1: {
			// test mode
			LOG.info("Start test mode!");
			MainFrame.instance().log("Start test mode!");

			Socket socket = null;
			Player user = null;
			BufferedInputStream bfin = null;
			try {
				while (!Thread.currentThread().isInterrupted()) {
					// blocking to get one socket
					while (!Thread.currentThread().isInterrupted()) {
						try {
							// block
							socket = this.sockets.take();
						} catch (InterruptedException e) {
							LOG.error(e);
							break;
						}

						try {
							socket.setSoTimeout(
									Integer.valueOf(ServerProperties.instance().getProperty("step.timeout")));
						} catch (SocketException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						try {
							bfin = new BufferedInputStream(socket.getInputStream());
						} catch (IOException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						try {
							// block
							bfin.read(buffer);
						} catch (IOException e) {
							LOG.error(e);
							try {
								socket.close();
							} catch (IOException e1) {
								LOG.error(e);
							}
							continue;
						}

						String msg = new String(buffer);
						Arrays.fill(buffer, (byte) 0);  // clear buffer
						if (msg.charAt(0) == 'A') {
							String id = msg.substring(1, 10);
							String password = msg.substring(10, 16);
							LOG.info("Accepted " + id);
							MainFrame.instance().log("Accepted " + id);
							if (Players.isContainedPlayer(id)) {
								user = Players.getPlayer(id);
								if (user.getPassword().equals(password)) {
									try {
										user.initial(socket, bfin);
									} catch (IOException e) {
										LOG.error(e);
										user.clear();
										socket = null;
										bfin = null;
										continue;
									}
									LOG.info("Welcome " + id);
									MainFrame.instance().log("Welcome " + id);
									break;
								} else {
									try {
										bfin.close();
										socket.close();
										socket = null;
										bfin = null;
									} catch (IOException e) {
										LOG.error(e);
									}
									LOG.info("Password is wrong: " + password);
									MainFrame.instance().log("Password is wrong: " + password);
								}
							} else {
								try {
									bfin.close();
									socket.close();
									socket = null;
									bfin = null;
								} catch (IOException e) {
									LOG.error(e);
								}
								LOG.info("No authorized " + id);
								MainFrame.instance().log("No authorized " + id);
							}
						} else {
							try {
								bfin.close();
								socket.close();
								socket = null;
								bfin = null;
							} catch (IOException e) {
								LOG.error(e);
							}
							LOG.info("Authorize msg format error");
							MainFrame.instance().log("Authorize msg format error");
						}
					}

					if (!Thread.currentThread().isInterrupted()) {
						// create contest service
						TestServiceRunnable tsr = new TestServiceRunnable(user);
						this.executor.execute(tsr);
					}
				}
			} finally {

			}
			break;
		}
		case 2:
			// debug mode
			LOG.info("Start debug mode!");
			MainFrame.instance().log("Start debug mode!");
			break;
		default:
			LOG.error("No this server mode!");
			System.exit(0);
			break;
		}
	}
}
