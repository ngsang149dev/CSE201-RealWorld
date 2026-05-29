# Taxi Fleet Expansion

## Problem Statement

Company V is a startup taxi company that wants to launch bus-like fixed routes across
Vietnamese cities (for example: Ho Chi Minh City → Vũng Tàu, Hà Nội → Hải Phòng).

To operate a route, Company V must own a vehicle of the right type for that route.
For example:
- A mountain route needs a **pickup truck**
- A highway route needs a **7-seat MPV**
- A city route needs a **4-seat sedan**

One vehicle can only serve one route at a time. So if two routes both need a 4-seat
sedan, Company V must buy **two** sedans.

Company V has a fixed budget of **W million VND** for buying vehicles. They do not
have to spend it all in one month — they can spread purchases across **T months**.

The catch: **vehicle prices can change at certain months.** If no change is announced,
the price stays the same as the previous month. When a change does happen, it applies
to the current price and **stays in effect from that month onward.**

For example: a car costs 100M in months 1 and 2 (no change). In month 3, the price
goes up 20% → it becomes 120M. From month 3 onward, the price is 120M — not 100M.
If month 5 then adds another 10%, it becomes 132M (10% of 120M, not of 100M).

**Goal:** Decide *which vehicles to buy* and *in which month* so that Company V can
open as many routes as possible without going over budget W.

---

## Input Format

**Line 1:** Five space-separated integers: `W T N R Q`

| Symbol | Meaning |
|--------|---------|
| `W` | Total budget in million VND |
| `T` | Number of months in the planning period |
| `N` | Number of vehicle types |
| `R` | Number of routes Company V wants to open |
| `Q` | Number of price-change events |

**Next N lines:** The base price of each vehicle type at month 1.
Line `i` contains one integer — the price of vehicle type `i` in million VND.

**Next R lines:** The vehicle type required by each route.
Line `i` contains one integer — the vehicle type number (between 1 and N) that route `i` needs.

**Next Q lines:** Each price-change event, written as three integers on one line: `M K P`

| Symbol | Meaning |
|--------|---------|
| `M` | The month this change takes effect |
| `K` | Which vehicle type is affected |
| `P` | Percentage to change the price by (positive = price goes up, negative = price goes down) |

> Each vehicle type has at most one price change per month.
> Events may be listed in any order.

---

## Output Format

Print a single integer: the **maximum number of routes** Company V can open
without exceeding the budget W.

---

## Constraints

| Variable | Limit |
|----------|-------|
| Budget `W` | 1 ≤ W ≤ 10^12 |
| Months `T` | 1 ≤ T ≤ 24 |
| Vehicle types `N` | 1 ≤ N ≤ 15 |
| Routes `R` | 1 ≤ R ≤ 500 |
| Price-change events `Q` | 0 ≤ Q ≤ N × T |
| Base price of any vehicle | 1 ≤ price ≤ 10^6 (million VND) |
| Percentage change `P` | −50 ≤ P ≤ 100 |

**Guaranteed by the problem:**
- All prices stay positive throughout all T months.
- **All prices after applying percentage changes are exact integers (no decimals).**
  The input is constructed so that `currentPrice × (100 + P)` is always divisible
  by 100 at every step. You do not need to handle rounding — integer division is exact.

**Time limit:** 1 second &nbsp;|&nbsp; **Memory limit:** 256 MB

---

## Sample Test Cases

### Sample 1

**Input:**
```
1010 4 2 3 4
400
600
1
1
2
2 1 -30
3 1 80
2 2 25
4 2 -40
```

**Output:**
```
3
```

**Explanation:**

Reading the input:
- `W=1010, T=4, N=2, R=3, Q=4`
- Month 1 prices: Type 1 = 400M, Type 2 = 600M
- Routes: Route 1 → Type 1, Route 2 → Type 1, Route 3 → Type 2
- Price changes: Type 1 drops 30% at month 2, then rises 80% at month 3.
  Type 2 rises 25% at month 2, then drops 40% at month 4.

Price table (each change applies to the price already in effect):

| Month | Type 1 | Type 2 |
|-------|--------|--------|
| 1     | 400M   | 600M   |
| 2     | 280M &nbsp;(−30% of 400) | 750M &nbsp;(+25% of 600) |
| 3     | 504M &nbsp;(+80% of 280) | 750M   |
| 4     | 504M   | 450M &nbsp;(−40% of 750) |

Cheapest price across all months:
- Type 1 → **280M** at month 2 *(not month 1!)*
- Type 2 → **450M** at month 4 *(not month 1!)*

Route costs at best prices: Route 1 → 280M, Route 2 → 280M, Route 3 → 450M

Greedy with W = 1010M:
- Open Route 1: −280M → 730M left ✓
- Open Route 2: −280M → 450M left ✓
- Open Route 3: −450M → 0M left ✓

> ⚠️ **Common mistake:** Using month-1 prices (400M + 400M + 600M = 1400M > 1010M)
> leads to a wrong answer of 2. The cheapest buying window for both types is NOT month 1.

---

### Sample 2

**Input:**
```
850 3 1 3 2
500
1
1
1
2 1 40
3 1 -40
```

**Output:**
```
2
```

**Explanation:**

Reading the input:
- `W=850, T=3, N=1, R=3, Q=2`
- Month 1 price: Type 1 = 500M
- All 3 routes need Type 1
- Price changes: Type 1 goes up 40% at month 2, then down 40% at month 3

Price table:

| Month | Type 1 |
|-------|--------|
| 1     | 500M   |
| 2     | 700M &nbsp;(+40% of 500) |
| 3     | 420M &nbsp;(−40% of 700) |

Cheapest price: **420M** at month 3.

Route costs: 420M, 420M, 420M → total for all 3 = 1260M > 850M

Greedy with W = 850M:
- Open Route 1: −420M → 430M left ✓
- Open Route 2: −420M → 10M left ✓
- Open Route 3: needs 420M, only 10M left ✗

> ⚠️ **Common mistake:** Assuming +40% then −40% cancels out, leaving the price at 500M.
> This gives a wrong cheapest price of 500M → only 1 route fits (500M ≤ 850M but
> 500M + 500M = 1000M > 850M). The correct calculation: 700 × 0.6 = **420M**, not 500M.
