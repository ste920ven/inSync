package com.example.insync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This thread runs during a connection with a remote device. It handles all
 * incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {
	// Debugging
	private static final String TAG = "BluetoothChatService";
	private static final boolean D = true;

	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	private Handler handler;

	public ConnectedThread(BluetoothSocket socket, Handler handler) {
		Log.d(TAG, "create ConnectedThread: ");
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		this.handler = handler;

		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			Log.e(TAG, "temp sockets not created", e);
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	public void run() {
		Log.i(TAG, "BEGIN mConnectedThread");
		byte[] buffer = new byte[1024];
		int bytes;

		// Keep listening to the InputStream while connected
		while (true) {
			try {
				// Read from the InputStream
				bytes = mmInStream.read(buffer);

				// Send the obtained bytes to the UI Activity
				Message s = handler.obtainMessage(BluetoothGuest.MESSAGE_READ,
						bytes, -1, buffer);
				s.sendToTarget();
				System.out.println(s.toString());
			} catch (IOException e) {
				Log.e(TAG, "disconnected", e);
			}
		}
	}

	/**
	 * Write to the connected OutStream.
	 * 
	 * @param buffer
	 *            The bytes to write
	 */
	public void write(byte[] buffer) {
		try {
			mmOutStream.write(buffer);

			// Share the sent message back to the UI Activity
			handler.obtainMessage(BluetoothHost.MESSAGE_WRITE, -1, -1, buffer)
					.sendToTarget();
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
	}

	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
			Log.e(TAG, "close() of connect socket failed", e);
		}
	}
}