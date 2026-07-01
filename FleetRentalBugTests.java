package com.example.fleet.service;

import com.example.fleet.domain.AccountStatus;
import com.example.fleet.domain.Customer;
import com.example.fleet.domain.CustomerTier;
import com.example.fleet.domain.Vehicle;
import com.example.fleet.repository.CustomerRepository;
import com.example.fleet.repository.RentalRepository;
import com.example.fleet.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INFS4202 - Software Testing & Quality Assurance, Spring 2026
 * Task 4 - Test Implementation (JUnit)
 *
 * These 3 tests are taken straight from the Task 3 Test Case Matrix
 * (test case IDs VEH-08, CUS-07 and REN-09). Each one targets one of
 * the bugs found during reverse engineering and test design, written as
 * a plain unit test with no Spring context and no mocks — just the real
 * service classes with their real repository objects.
 *
 * NOTE: all 3 tests below are EXPECTED TO FAIL. That is the point —
 * they are written to match what the app SHOULD do, and the app
 * currently does not do that, which is exactly how these bugs were
 * identified. The failing assertion message printed by JUnit for each
 * test shows the actual buggy value next to the expected correct value.
 *
 * Bugs targeted:
 *   VEH-08 -> Bug #8  (available units can exceed total units)
 *   CUS-07 -> Bug #7  (blocking threshold uses strict > instead of >=)
 *   REN-09 -> Bug #3  (late charge day-count off by one)
 */
class FleetRentalBugTests {

    // ---------------------------------------------------------------
    // Test Case VEH-08 (white-box) -> Bug #8
    //
    // VehicleService.save() never checks that availableUnits stays
    // within totalUnits, so editing a vehicle can push its available
    // units above its total units.
    // ---------------------------------------------------------------
    @Test
    void editingVehicleShouldNotAllowAvailableUnitsToExceedTotalUnits() {
        VehicleRepository vehicleRepository = new VehicleRepository();
        RentalRepository rentalRepository = new RentalRepository();
        VehicleService vehicleService = new VehicleService(vehicleRepository, rentalRepository);

        Vehicle vehicle = new Vehicle(null, "Civic", "Honda", 2022, "Sedan", 2, 2);
        vehicleService.save(vehicle);

        // Someone edits the vehicle and (by mistake or on purpose) sets
        // available units higher than total units (2).
        vehicle.setAvailableUnits(5);
        Vehicle saved = vehicleService.save(vehicle);

        assertThat(saved.getAvailableUnits())
                .as("available units should never be greater than total units")
                .isLessThanOrEqualTo(saved.getTotalUnits());
    }

    // ---------------------------------------------------------------
    // Test Case CUS-07 (white-box) -> Bug #7
    //
    // CustomerService.updateAccountStatus() uses a strict "greater than"
    // check against the blocking threshold, so a customer sitting at
    // EXACTLY the threshold ($50.00) is not blocked.
    // ---------------------------------------------------------------
    @Test
    void customerAtExactBlockingThresholdShouldBeBlocked() {
        CustomerRepository customerRepository = new CustomerRepository();
        RentalRepository rentalRepository = new RentalRepository();
        SettingsService settingsService = new SettingsService();
        CustomerService customerService = new CustomerService(
                customerRepository, rentalRepository, settingsService);

        // Default blocking threshold (see AppSettings) is $50.00.
        Customer customer = new Customer(
                null, "Jane Doe", "jane@example.com",
                CustomerTier.INDIVIDUAL, new BigDecimal("50.00"), AccountStatus.ACTIVE);

        customerService.updateAccountStatus(customer);

        assertThat(customer.getAccountStatus())
                .as("a customer exactly at the blocking threshold should be blocked")
                .isEqualTo(AccountStatus.BLOCKED);
    }

    // ---------------------------------------------------------------
    // Test Case REN-09 (white-box) -> Bug #3
    //
    // LateChargeCalculator subtracts 1 extra day from the real number of
    // days late, so a 3-day-late return only gets charged for 2 days.
    // We use the INDIVIDUAL tier here on purpose, so the only thing being
    // tested is the day-count bug (Bug #3), not the separate walk-in
    // rate bug (Bug #2).
    // ---------------------------------------------------------------
    @Test
    void lateChargeShouldChargeForEveryFullDayLate() {
        SettingsService settingsService = new SettingsService();
        LateChargeCalculator calculator = new LateChargeCalculator(settingsService);

        LocalDate returnDueDate  = LocalDate.of(2026, 1, 1);
        LocalDate actualReturnDate = LocalDate.of(2026, 1, 4); // 3 days late

        BigDecimal lateCharge = calculator.calculateLateCharge(
                returnDueDate, actualReturnDate, CustomerTier.INDIVIDUAL);

        // Individual rate is $1.00/day (see AppSettings) x 3 days late = $3.00
        assertThat(lateCharge)
                .as("3 days late at $1.00/day should charge $3.00")
                .isEqualByComparingTo(new BigDecimal("3.00"));
    }
}
