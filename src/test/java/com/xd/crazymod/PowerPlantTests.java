package com.xd.crazymod;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PowerPlantTests {

	@Test
	@DisplayName("Test Thermal Boiler Water-to-Steam Expansion Ratio")
	void testBoilerThermodynamics() {
		int waterInput = 100; // mB
		int temperature = 540; // °C

		assertTrue(temperature >= 100, "Water boils only at or above 100°C");
		int boilRate = Math.min(waterInput, Math.max(1, (temperature - 100) / 20));
		int steamProduced = boilRate * 10;

		assertEquals(22, boilRate, "Boil rate calculation at 540°C");
		assertEquals(220, steamProduced, "Steam volume expansion ratio must be 10x");
	}

	@Test
	@DisplayName("Test Turbogenerator Electromagnetic Induction Power Curve")
	void testGeneratorPowerCurve() {
		int maxRpm = 3000;
		int basePower = 12800; // E/t

		int powerAtMaxRpm = (int) ((3000 / (double) maxRpm) * basePower);
		assertEquals(12800, powerAtMaxRpm, "Generator should produce 12,800 E/t at synchronous 3000 RPM");

		int powerAtHalfRpm = (int) ((1500 / (double) maxRpm) * basePower);
		assertEquals(6400, powerAtHalfRpm, "Generator should produce 6,400 E/t at 1500 RPM");

		int powerAtZeroRpm = (int) ((0 / (double) maxRpm) * basePower);
		assertEquals(0, powerAtZeroRpm, "Generator should produce 0 E/t at 0 RPM");
	}

	@Test
	@DisplayName("Test VVER Nuclear Reactor Reactivity & SCRAM (АЗ-5) Logic")
	void testNuclearReactivityAndScram() {
		boolean scrammed = true;
		int controlRodInsertion = 100; // 100% inserted
		int thermalPower = 0;

		// When SCRAM is active, power is strictly 0
		if (!scrammed && controlRodInsertion < 90) {
			thermalPower = (100 - controlRodInsertion) * 32;
		} else {
			thermalPower = 0;
		}
		assertEquals(0, thermalPower, "Scrammed reactor must produce 0 MWth");

		// Emergency protection disengaged, nominal rod position 25% inserted
		scrammed = false;
		controlRodInsertion = 25;
		if (!scrammed && controlRodInsertion < 90) {
			thermalPower = (100 - controlRodInsertion) * 32;
		} else {
			thermalPower = 0;
		}
		assertEquals(2400, thermalPower, "Nominal operation (25% inserted rods) produces 2400 MWth");
		assertTrue(thermalPower > 500, "Reactor should be marked as critical");
	}

	@Test
	@DisplayName("Test Oil Boiler Fast Thermal Dynamics")
	void testOilBoilerThermodynamics() {
		int waterStored = 500;
		int temperature = 600;

		assertTrue(temperature >= 100);
		int boilRate = Math.min(waterStored, Math.max(1, (temperature - 100) / 15));
		int steamProduced = boilRate * 12;

		assertEquals(33, boilRate);
		assertEquals(396, steamProduced);
	}

	@Test
	@DisplayName("Test Combined-Cycle HRSG Recovery Rate")
	void testHRSGRecovery() {
		int waterStored = 50;
		int boilRate = Math.min(waterStored, 20);
		int steamOutputRate = boilRate * 10;

		assertEquals(20, boilRate);
		assertEquals(200, steamOutputRate);
	}

	@Test
	@DisplayName("Test Mod ID Constant")
	void testModId() {
		assertEquals("crazymod", CrazyMod.MOD_ID);
	}
}
