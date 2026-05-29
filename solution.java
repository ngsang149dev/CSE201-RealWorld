import java.util.*;

public class solution {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // -------------------------------------------------------
        // Step 1: Read W, T, N, R, Q
        // -------------------------------------------------------
        long W = sc.nextLong();
        int T = sc.nextInt();
        int N = sc.nextInt();
        int R = sc.nextInt();
        int Q = sc.nextInt();

        // -------------------------------------------------------
        // Step 2: Read base prices for each vehicle type
        // basePrice[i] = price of vehicle type (i+1) at month 1
        // -------------------------------------------------------
        long[] basePrice = new long[N];
        for (int i = 0; i < N; i++) {
            basePrice[i] = sc.nextLong();
        }

        // -------------------------------------------------------
        // Step 3: Read route requirements
        // routeType[j] = which vehicle type route j needs (1-indexed)
        // -------------------------------------------------------
        int[] routeType = new int[R];
        for (int j = 0; j < R; j++) {
            routeType[j] = sc.nextInt();
        }

        // -------------------------------------------------------
        // Step 4: Read price-change events into HashMaps
        // priceEvents.get(i) maps { month -> percentage change }
        // for vehicle type (i+1)
        // -------------------------------------------------------
        List<HashMap<Integer, Integer>> priceEvents = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            priceEvents.add(new HashMap<>());
        }

        for (int q = 0; q < Q; q++) {
            int month = sc.nextInt();
            int type  = sc.nextInt();   // 1-indexed
            int pct   = sc.nextInt();
            priceEvents.get(type - 1).put(month, pct);
        }

        // -------------------------------------------------------
        // Step 5: Find the cheapest price for each vehicle type
        // Simulate months 1 -> T, track running minimum
        // -------------------------------------------------------
        long[] bestPrice = new long[N];

        for (int i = 0; i < N; i++) {
            long currentPrice = basePrice[i];
            bestPrice[i] = currentPrice;            // month 1 is the starting point

            for (int month = 2; month <= T; month++) {
                if (priceEvents.get(i).containsKey(month)) {
                    int pct = priceEvents.get(i).get(month);
                    currentPrice = currentPrice * (100 + pct) / 100;
                }
                if (currentPrice < bestPrice[i]) {
                    bestPrice[i] = currentPrice;
                }
            }
        }

        // -------------------------------------------------------
        // Step 6: Assign a cost to each route
        // cost = bestPrice of the vehicle type that route needs
        // -------------------------------------------------------
        long[] routeCost = new long[R];
        for (int j = 0; j < R; j++) {
            routeCost[j] = bestPrice[routeType[j] - 1];
        }

        // -------------------------------------------------------
        // Step 7: Sort route costs ascending
        // -------------------------------------------------------
        Arrays.sort(routeCost);

        // -------------------------------------------------------
        // Step 8: Greedy — pick cheapest routes first
        // Count how many fit within budget W
        // -------------------------------------------------------
        long spent = 0;
        int count  = 0;

        for (int j = 0; j < R; j++) {
            if (spent + routeCost[j] <= W) {
                spent += routeCost[j];
                count++;
            } else {
                break;
            }
        }

        System.out.println(count);
        sc.close();
    }
}