package com.prem.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prem.server.config.Configuration;

public class HttpConnectionWorkerThread extends Thread {

	static final String CRLF = "\r\n";
	static String html = " <html> <body> <h1>PREM</h1> </body> </html> ";
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
	static private String responce = "HTTP/1.1 200 OK" + CRLF // status
			+ "ContetntLenght: " + html.getBytes().length + CRLF // Header
			+ CRLF
			+ html + CRLF + CRLF;

	Configuration conf;
	ServerSocket serverSocket;
	private String workerName;
	private Socket socket;

	public HttpConnectionWorkerThread(Socket socket, String workerName) {
		this.workerName = workerName;
		this.socket = socket;
	}

	@Override
	public void run() {

		try (InputStream iStream = socket.getInputStream();
				OutputStream oStream = socket.getOutputStream()) {

			int _bytes;

			while ((_bytes = iStream.read()) >= 0) {
				System.out.print((char) _bytes);
			}

			oStream.write(responce.getBytes());

			LOGGER.info("Conection Prossesing is Finished");

			iStream.close();
			oStream.close();

		} catch (IOException e) {
			LOGGER.error("Problem in communicaton");

			e.printStackTrace();
		}

	}

}
