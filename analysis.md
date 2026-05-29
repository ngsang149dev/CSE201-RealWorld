# Problem Analysis and Design

## Real-World Context

Taxi and bus companies in Vietnam (such as Phương Trang or Hoàng Long) do not buy
their entire fleet at once. They operate under a fixed annual capital budget and purchase
vehicles in batches across several months, timing purchases around market conditions —
import tariff changes, USD/VND exchange rate fluctuations, and manufacturer promotions.

Each fixed route in their network requires a vehicle of a specific type. A mountain route
needs a high-clearance pickup; a highway express route needs a 16-seat coach. Since each
purchased vehicle can only serve one route, the company must acquire enough vehicles of
each type to cover as many routes as possible without overspending.

This assignment models that planning decision: given a fixed budget, a set of candidate
routes, and a vehicle market where prices shift month-to-month, determine the maximum
number of routes the company can put into operation.

---

## Problem Modeling

### Entities and Variables

| Real-World Concept | Variable | Type |
|--------------------|----------|------|
| Total capital budget | `W` | `long` |
| Planning horizon | `T` months | `int` |
| Vehicle categories | `N` types | `int` |
| Candidate routes | `R` routes | `int` |
| Market events | `Q` price changes | `int` |
| Base price of type `i` | `basePrice[i]` | `long[]` |
| Required type for route `j` | `routeType[j]` | `int[]` |
| Price changes per type | `events.get(i)` | `List<HashMap<Integer,Integer>>` |

### Key Observations That Shape the Model

**Observation 1 — Purchase timing is flexible.**
The company can buy any vehicle in any month. The only constraint is total spend ≤ W.
This means we only care about the *cheapest month* for each vehicle type, not which
specific month was chosen.

**Observation 2 — Each route contributes equally.**
Opening one route always adds exactly 1 to the answer, regardless of which route it is.
No route is "worth more" than another. This rules out Knapsack (which handles varying
values) and points directly to Greedy.

**Observation 3 — Prices are independent across types.**
A price change for Type 1 does not affect Type 2. So the best buying price for each
type can be found independently.

### Reduction to a Simpler Problem

After applying the three observations above, the original problem reduces to:

> Given a list of costs (one per route, derived from the cheapest available price of
> the required vehicle type), select the maximum number of items whose total ≤ W.

This is the classic **"maximize count under budget"** problem, solved optimally by
sorting costs in ascending order and greedily selecting from cheapest to most expensive.

---

## Expected Solution

The core algorithm is identical in both approaches below. The difference is purely
in how data is organized in code — primitive arrays vs. user-defined classes.
Both pass the time limit comfortably given the problem constraints.

---

### Shared Algorithm (both approaches follow these 4 steps)

**Step 1 — Build the price event map.**

Read all Q events. For each vehicle type, store a mapping of `{month → percentage change}`
so that price lookups during simulation are O(1) per month.

```
for each event (M, K, P):
    attach P to vehicle type K at month M
```

Time: O(Q)

---

**Step 2 — Find the cheapest price for each vehicle type.**

Simulate months 1 through T. At each month, apply the percentage change if one exists,
then track the running minimum price seen so far.

```
for each vehicle type i:
    currentPrice = basePrice[i]
    bestPrice[i] = basePrice[i]        ← month 1 is the starting candidate
    for month = 2 to T:
        if an event exists for (i, month):
            currentPrice = currentPrice * (100 + pct) / 100
        if currentPrice < bestPrice[i]:
            bestPrice[i] = currentPrice
```

Time: O(N × T)

---

**Step 3 — Assign a cost to each route.**

Each route needs exactly one vehicle of its required type. Its cost is the cheapest
price found for that type in Step 2.

```
for each route j:
    routeCost[j] = bestPrice[ requiredType[j] ]
```

Time: O(R)

---

**Step 4 — Sort and apply Greedy.**

Sort all route costs ascending. Accumulate from cheapest to most expensive, counting
how many routes fit within budget W.

```
sort(routeCosts)
spent = 0, count = 0
for each cost in routeCosts:
    if spent + cost <= W:
        spent += cost
        count++
    else:
        break
return count
```

Time: O(R log R)

---

### Why Greedy Is Optimal Here

**Claim:** Selecting routes cheapest-first always yields the maximum route count.

**Proof sketch:** Suppose an optimal solution skips a cheap route X and includes a
more expensive route Y. Swapping Y for X keeps the count identical but reduces total
spend — potentially freeing budget to add even more routes. Therefore no optimal
solution benefits from skipping a cheaper route. Cheapest-first is always at least
as good as any alternative order. ∎

---

### Approach 1 — Primitive Arrays (submitted solution)

Data is stored in raw `long[]` and `int[]`. Price events are held in a
`List<HashMap<Integer, Integer>>` indexed by vehicle type.

```java
long[]   basePrice  = new long[N];
long[]   bestPrice  = new long[N];
int[]    routeType  = new int[R];
long[]   routeCost  = new long[R];
List<HashMap<Integer, Integer>> priceEvents = new ArrayList<>();
```

**Sorting:** `Arrays.sort(routeCost)` — operates directly on primitives, no boxing.

| Phase | Time | Space |
|-------|------|-------|
| Read events | O(Q) | O(Q) |
| Compute bestPrice | O(N × T) | O(N) |
| Assign route costs | O(R) | O(R) |
| Sort | O(R log R) | O(1) extra |
| Greedy | O(R) | O(1) extra |
| **Total** | **O(Q + N×T + R log R)** | **O(Q + N + R)** |

**Why this was chosen as the submitted solution:**
- Fastest in practice — `long[]` avoids object allocation and unboxing overhead
- Shortest code — easiest to verify correct during a timed submission
- `Arrays.sort()` on primitives uses a dual-pivot Quicksort, slightly faster than
  the TimSort used for object arrays

---

### Approach 2 — User-Defined Classes (alternative)

Two classes encapsulate the domain concepts: `Vehicle` holds price simulation logic,
`Route` is a plain data class. Sorting is handled by a **lambda expression written
directly in `main`** — no `Comparable` interface needed.

```java
class Vehicle {
    int  type;
    long basePrice;
    long bestPrice;
    HashMap<Integer, Integer> events;   // month -> pct

    void addEvent(int month, int pct) { events.put(month, pct); }

    void computeBestPrice(int T) {
        long current = basePrice;
        for (int month = 2; month <= T; month++) {
            if (events.containsKey(month))
                current = current * (100 + events.get(month)) / 100;
            if (current < bestPrice) bestPrice = current;
        }
    }
}

class Route {
    int  id;
    long cost;

    Route(int id, long cost) {
        this.id   = id;
        this.cost = cost;
    }
}
```

**Sorting with lambda in main — Route stays a plain class:**

```java
// No Comparable needed — comparison logic lives here, not in the class
routes.sort((a, b) -> Long.compare(a.cost, b.cost));
```

| Phase | Time | Space |
|-------|------|-------|
| Read events | O(Q) | O(Q) |
| Compute bestPrice | O(N × T) | O(N) |
| Assign route costs | O(R) | O(R) |
| Sort | O(R log R) | O(R) extra for object list |
| Greedy | O(R) | O(1) extra |
| **Total** | **O(Q + N×T + R log R)** | **O(Q + N + R)** |

**Trade-offs vs. Approach 1:**
- `ArrayList<Route>` stores object references → slightly more memory than `long[]`
- `Long` unboxing in HashMap adds a small constant overhead per lookup
- With R ≤ 500 and N ≤ 15, the difference is microseconds — both pass in well
  under 1 second
- Code is more readable and easier to extend (e.g., adding a route name or priority
  field requires only one line in the `Route` class)

---

### Summary Comparison

| Criterion | Approach 1 (Arrays) | Approach 2 (Classes) |
|-----------|--------------------|--------------------|
| Time complexity | O(Q + N×T + R log R) | O(Q + N×T + R log R) |
| Space complexity | O(Q + N + R) | O(Q + N + R) |
| Passes time limit | ✅ Yes | ✅ Yes |
| Code length | ~60 lines | ~90 lines |
| Readability | Moderate | High |
| Extensibility | Low | High |
| **Submitted?** | **✅ Yes** | For reference only |

---

## Test Case Design

Test cases are grouped into four tiers to cover the full spectrum of inputs:

### Tier 1 — Basic Cases (cases 1–5)
Straightforward inputs where the correct answer is immediately verifiable by hand.
These confirm the solution handles normal flow correctly.

- Single vehicle type, all prices constant (Q = 0)
- Two vehicle types, one price change each
- Budget exactly covers K routes (no remainder)
- All routes need the same vehicle type
- Only one route exists

### Tier 2 — Boundary Cases (cases 6–12)
Inputs at or near the limits of each variable.

- W = 1 (cannot afford any vehicle → output 0)
- T = 1 (no price changes can ever apply → always use base price)
- T = 24, N = 15, R = 500, Q = 360 (maximum size stress test)
- Budget exactly equal to the sum of all route costs (all routes fit)
- Budget one unit less than needed for the next route

### Tier 3 — Edge Cases (cases 13–20)
Inputs that expose common implementation mistakes.

- Q = 0: no price changes at all — bestPrice = base price for all types
- Price rises every month: cheapest is always month 1
- Price falls every month: cheapest is always month T
- Price rises then falls below original (the +40% / −40% trap)
- Price falls then rises above original
- All routes require a type whose best price exceeds W → output 0
- R = 1: only one route, either affordable or not
- Multiple types with identical best prices

### Tier 4 — Stress Tests (cases 21–30)
Large inputs designed to catch O(N² × T) or O(R²) solutions that would TLE.

- N = 15, T = 24, R = 500, Q = 360 with all prices near 10^6 and W near 10^12
- W very large (afford everything): output = R
- W = 0: output = 0
- Many routes sharing the same cheapest vehicle type
- Prices zigzag up and down every month — minimum is deep in the middle

---

## Edge Cases

| Edge Case | Expected Behavior | Why It Matters |
|-----------|-------------------|----------------|
| Q = 0 (no events) | Use base prices directly; bestPrice = basePrice | Ensures the event loop handles an empty HashMap gracefully |
| W cannot afford any vehicle | Output `0` | Greedy loop must handle zero selections |
| W affords all routes | Output `R` | Greedy must not stop early |
| +P% then −P% compound | Final price ≠ original price | 100 × 1.4 × 0.6 = 84, not 100 — integer arithmetic must be applied correctly |
| Multiple events same month same type | Guaranteed not to occur by problem constraints | No special handling needed, but worth noting |
| Price at month T is the global minimum | bestPrice found only at the last iteration | Ensures the month loop runs all the way to T |
| All routes need the most expensive type | Budget may cover fewer routes than R | Tests that route cost assignment uses per-type best price, not a global minimum |