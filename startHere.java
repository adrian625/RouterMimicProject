import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class routerData {
	String routerAdd = "afsaccess1.njit.edu";
	boolean listenInUse = false;
	public String recMessage;
	public static String routePath;
	public static boolean inUse = false;
}

class routerCT {
	String rd = new routerData().routerAdd;
	int route0, route1, route2;

	public routerCT() {
		if (rd == new addressData(rd).router0) {
			route0 = 1;
			route1 = 2;
			route2 = 3;
		}
		if (rd == new addressData(rd).router1) {
			route0 = 2;
			route2 = 0;
		}
		if (rd == new addressData(rd).router2) {
			route0 = 1;
			route1 = 3;
			route2 = 0;
		}
		if (rd == new addressData(rd).router3) {
			route0 = 0;
			route2 = 2;
		}
	}
}

class addressData {
	String router0 = "afsaccess1.njit.edu", router1 = "afsaccess2.njit.edu", router2 = "afsaccess3.njit.edu",
			router3 = "afsaccess4.njit.edu";
	// 9 is used for not connected so as to not cause issues by having null
	int cost0 = 9, cost1 = 9, cost2 = 9;
	int port = 60000;
	int routerNum;
	String routerString;
	String idRouter;
	int routerCost;

	public addressData(int p, String i) {
		if (p == 0) {

		}
	}

	public addressData(int x) {
		if (x == 0) {
			idRouter = router0;
		}
		if (x == 1) {
			idRouter = router1;
		}
		if (x == 2) {
			idRouter = router2;
		}
		if (x == 3) {
			idRouter = router3;
		}
	}

	public addressData(String i) {
		if (i == router0) {
			routerString = "0137";
			routerNum = 0;
			cost0 = 1;
			cost1 = 3;
			cost2 = 7;
		}
		if (i == router1) {
			routerNum = 1;
			routerString = "1019";
			cost0 = 1;
			cost2 = 1;
		}
		if (i == router2) {
			routerNum = 2;
			routerString = "3102";
			cost0 = 1;
			cost1 = 2;
			cost2 = 3;
		}
		if (i == router3) {
			routerNum = 3;
			routerString = "7920";
			cost0 = 7;
			cost2 = 2;
		}

	}

}

class serverThread extends startHere implements Runnable {
	Socket s = new Socket();
	String m = "";
	ServerSocket listeningSocket;

	public void run() {
		while (true) {
			try {
				listeningSocket = new ServerSocket(60000);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				s = listeningSocket.accept();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				listeningSocket.close();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			try {
				InputStreamReader isr = new InputStreamReader(s.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				m = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			calcThread ct = new calcThread(m);
			Thread too = new Thread(ct);
			too.start();
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}

class calcThread extends startHere implements Runnable {

	int dealingRouter;
	String routerString;
	String newString;

	public calcThread(String s) {
		dealingRouter = s.indexOf("0");
		routerString = s;
	}

	public void run() {
		while (routerData.inUse) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {

			}
		}
		routerData.inUse = true;
    String oldString = routerData.routePath;
		newString = "";
		for (int i = 0; i < routerString.length(); i++) {
			if (Character.getNumericValue(routerString.charAt(i))
					+ Character.getNumericValue(routerData.routePath.charAt(dealingRouter)) < Character
							.getNumericValue(routerData.routePath.charAt(i))) {
				newString += Integer.toString((Character.getNumericValue(routerString.charAt(i))
						+ Character.getNumericValue(routerData.routePath.charAt(dealingRouter))));
			} else {
				newString += Integer.toString((Character.getNumericValue(routerData.routePath.charAt(i))));
			}
		}
		if(Integer.parseInt(newString) != Integer.parseInt(oldString)){
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			for (int i = 0; i < newString.length(); i++) {
				System.out.println("Connection to Router #" + i + " is :" + newString.charAt(i));
			}
		}
		routerData.routePath = newString;
		routerData.inUse = false;
	}
}

class sendEr extends startHere implements Runnable {
	Socket sendSocket;
	routerData rd = new routerData();
	addressData ad = new addressData(rd.routerAdd);
	String routerPath = "";

	public void run() {
		while (true) {

			while (routerData.inUse) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
			}
			routerData.inUse = true;
			routerPath = routerData.routePath;
			routerData.inUse = false;
			for (int i = 0; i < 4; i++) {
				addressData s = new addressData(i);
				String x = s.idRouter;
				if (!x.equals(routerData.routePath)) {
					boolean failed = true;
					while (failed) {
						try {
							sendSocket = new Socket(x, 60000);
							failed = false;
						} catch (IOException e) {
							// System.out.println("denied");
							failed = true;
						}
					}
					try {
						DataOutputStream outToServer = new DataOutputStream(sendSocket.getOutputStream());
						outToServer.writeBytes(routerPath);
					} catch (IOException e) {

					}
					try {
						sendSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
	}

}

public class startHere {

	public static void main(String arg[]) {
		routerData rd = new routerData();
		addressData ad = new addressData(rd.routerAdd);
		routerData.routePath = ad.routerString;
		serverThread r = new serverThread();
		sendEr y = new sendEr();
		Thread foo = new Thread(r);
		Thread boo = new Thread(y);
		foo.start();
		boo.start();
		String x = routerData.routePath;
		while (true) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
