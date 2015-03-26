package client.gta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileParser {

	public static List<GtaClient> getClientsData(String file) {
		List<GtaClient> clients = readFile(file);
		// System.out.println(clients);
		sort(clients);
		System.out.println(clients);
		return clients;
	}

	private static void sort(List<GtaClient> clients) {
		Collections.sort(clients, new Comparator<GtaClient>() {
			public int compare(GtaClient p1, GtaClient p2) {
				return p1.compareTo(p2);
			}
		});
	}

	private static List<GtaClient> readFile(String file) {
		List<GtaClient> clients = new ArrayList<GtaClient>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			// StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			int i = 0;

			while (line != null && i < 108) {
				// sb.append(line);
				// sb.append(System.lineSeparator());
				line = br.readLine();
				GtaClient res = parseLine(line);
				if (res != null) {
					clients.add(res);
				}
				i += 1;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return clients;
	}

	private static GtaClient parseLine(String line) {
		String[] temp = line.split(", ");

		if (temp.length >= 3) {
			int id = 0;
			try {
				id = Integer.parseInt(temp[0]);
				double timestamp = Double.parseDouble(temp[1]);
				double lifespan = Double.parseDouble(temp[2]);

				GtaClient client = new GtaClient(id, timestamp, lifespan);
				return client;

			} catch (Exception e) {

			}

		}
		return null;

	}

}
