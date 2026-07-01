# Campus Fleet & Rental Center — QA & Software Testing

**Course:** INFS4202 Software Testing & Quality Assurance — Spring 2026  
**University:** University of Doha for Science and Technology (UDST)  
**Team:** Osama Altaweel · Ahmed Ibrahim · Faisal Jabri  

---

## Project Overview

End-to-end QA project on a Java/Spring Boot fleet rental management system. Covered the full testing lifecycle: requirements reverse-engineering, test planning, manual functional testing, JUnit unit testing, bug reporting, and JMeter performance testing.

**Application Under Test:** Campus Fleet & Rental Center — a Spring Boot 3 web application managing vehicles, customers, and rental transactions.

---

## What Was Tested

| Module | Test Cases | Bugs Found |
|---|---|---|
| Vehicles | 10 | 3 |
| Customers | 10 | 4 |
| Rentals | 10 | 3 |
| **Total** | **30+** | **10** |

---

## Testing Approach

- **Black-box functional testing** — UI/workflow level, no code access
- **Black-box boundary testing** — edge values, exact thresholds, limit conditions
- **White-box unit testing** — direct service method calls via JUnit 5
- **Performance testing** — Apache JMeter load test on the rental creation workflow

---

## Key Deliverables

### Test Planning
- 22 functional requirements + 8 non-functional requirements reverse-engineered from the codebase
- Full test plan covering scope, approach, environment, schedule, and responsibilities
- Requirements Traceability Matrix mapping every test case to a functional requirement

### Test Design & Execution
- 30+ test cases across 3 modules (Vehicles, Customers, Rentals)
- Mix of positive, negative, boundary, and white-box test types
- 10 confirmed defects documented with severity, reproduction steps, and expected vs actual results

### JUnit Implementation (`FleetRentalBugTests.java`)
Three unit tests derived directly from the test case matrix, targeting confirmed bugs:

| Test | Bug Targeted | Assertion |
|---|---|---|
| `editingVehicleShouldNotAllowAvailableUnitsToExceedTotalUnits` | VEH-08 | `availableUnits ≤ totalUnits` |
| `customerAtExactBlockingThresholdShouldBeBlocked` | CUS-07 | Customer at $50.00 = BLOCKED |
| `lateChargeShouldChargeForEveryFullDayLate` | REN-09 | 3 days late × $1.00 = $3.00 |

> All 3 tests are intentionally failing — they document real bugs in the application where actual behavior diverges from expected behavior.

### Performance Testing (JMeter)
- **Scenario:** Rental creation workflow (most logic-heavy endpoint)
- **Load:** 20 concurrent virtual users × 10 loops = **800 total requests**
- **Results:** 0 errors, all response times under 200ms, well within the 1s target
- **Finding:** No bottleneck at this load level; flagged in-memory HashMap storage as a scalability risk for production

---

## Tech Stack

| Tool | Purpose |
|---|---|
| Java 17 + Spring Boot 3.2.0 | Application under test |
| JUnit 5 + AssertJ | Unit test implementation |
| Apache JMeter | Performance / load testing |
| Chrome 125 / Firefox | Manual functional testing |
| GitHub Issues | Bug tracking and defect logging |

---

## How to Run the JUnit Tests

```bash
# Clone the repo
git clone https://github.com/OsamaAltawel/campus-fleet-qa.git

# Run tests (requires Java 17 + Maven)
./mvnw test -Dtest=FleetRentalBugTests
```

> Expected: all 3 tests fail. This is intentional — the tests document confirmed bugs in the unpatched application.

---

## Full Report

See [`INFS4202_Project_Report.pdf`](./INFS4202_Project_Report.pdf) for the complete QA deliverable including:
- Reverse-engineered requirements list
- Class diagram
- Full test plan
- Test case matrix + traceability matrix
- JUnit execution screenshots
- Bug report log with evidence screenshots
- JMeter test plan, results table, and analysis

---

## Selected Bug Highlights

**Bug #3 — Late charge day-count off by one (REN-09)**  
`LateChargeCalculator` subtracts 1 extra day, so a 3-day-late return is only charged for 2 days. Financial impact on every overdue rental.

**Bug #7 — Blocking threshold uses strict greater-than (CUS-07)**  
`updateAccountStatus()` uses `> $50.00` instead of `>= $50.00`, so a customer at exactly the threshold is not blocked and can still create rentals.

**Bug #8 — Available units can exceed total units (VEH-08)**  
`VehicleService.save()` never validates that `availableUnits ≤ totalUnits`, allowing data corruption through the edit form.

---

*Part of a Bachelor of Information Systems degree at UDST, graduating 2027.*
