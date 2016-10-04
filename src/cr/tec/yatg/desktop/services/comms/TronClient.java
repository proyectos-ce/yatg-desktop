package cr.tec.yatg.desktop.services.comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by joseph on 10/3/16.
 */
public class TronClient {
	private static TronClient ourInstance = new TronClient();
	private String serverIp;
	private int serverPort;
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private ClientWrite write;
	private ClientRead read;
	private boolean running = false;

	private TronClient() {
	}

	public static TronClient getInstance() {
		return ourInstance;
	}

	public boolean connect(String ip, int port) {
		try {
			this.serverIp = ip;
			this.serverPort = port;
			this.socket = new Socket(serverIp, serverPort);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			return ping();

		} catch (IOException e) {
			return false;
		}
	}

	private boolean ping() {
		out.println("PING");
		long timeoutMs = 1;
		long timeoutExpiredMs = System.currentTimeMillis() + timeoutMs;
		try {
			String line;
			while (true) {
				if ((line = in.readLine()) != null) {
					return line.equals("PONG");
				}
				if (System.currentTimeMillis() >= timeoutExpiredMs) {
					return false;
				}
			}
		} catch (IOException e) {
			return false;
		}
	}

	public String joinIfCan(String name) {
		out.println("%J" + name);
		long timeoutMs = 1;
		long timeoutExpiredMs = System.currentTimeMillis() + timeoutMs;
		try {
			String line;
			while (true) {
				if ((line = in.readLine()) != null) {
					if (line.equals("OK")) {
						this.running = true;
						write = new ClientWrite(out);
						write.start();
						read = new ClientRead(in);
						read.start();
						return "OK";
					} else {
						return line;
					}
				}
				if (System.currentTimeMillis() >= timeoutExpiredMs) {
					return "El servidor no responde.";
				}
			}
		} catch (IOException e) {
			return "El servidor no responde.";
		}
	}

	public void send(String msg) {
		out.println(msg);
	}

	public boolean isRunning() {
		return running;
	}
}
