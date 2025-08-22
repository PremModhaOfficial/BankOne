package com.bank.server.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.server.config.Configuration;

public class ServerListnerThread extends Thread {

	private static Configuration conf;
	private static ServerSocket serverSocket;
	static private int workers = 0;
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerListnerThread.class);

	public ServerListnerThread(Configuration conf) throws IOException {
		ServerListnerThread.conf = conf;
		serverSocket = new ServerSocket(conf.getPort());
	}

	@Override
	public void run() {

		try {
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				LOGGER.info("connected via socket: " + socket);
				new HttpConnectionWorkerThread(socket, "Worker" + ++workers).start();
			}
		} catch (Exception e) {
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					LOGGER.error("Problem in Setting Socket", e);
				}
			}
		}

	}

	public static int getWorkers() {
		return workers;
	}
}
