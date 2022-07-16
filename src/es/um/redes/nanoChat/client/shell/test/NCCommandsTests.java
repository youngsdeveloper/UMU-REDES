package es.um.redes.nanoChat.client.shell.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.um.redes.nanoChat.client.shell.NCCommands;

class NCCommandsTests extends NCCommands {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testStringToCommand() 
	{
		// Test strintToCommand works for all commands
		assertTrue(IntStream.range(0, _valid_user_commands_str.length).allMatch(i -> 
			NCCommands.stringToCommand(NCCommands._valid_user_commands_str[i]) == NCCommands._valid_user_commands[i]));
	}		

	@Test
	void testStringToCommandCaseRegardless() 
	{
		// Test strintToCommand works for all commands, regardless of the case
		assertTrue(IntStream.range(0, _valid_user_commands_str.length).allMatch(i -> 
			NCCommands.stringToCommand(NCCommands._valid_user_commands_str[i].toUpperCase()) == NCCommands._valid_user_commands[i]));
	}		

	@Test
	void testStringToCommandShouldFail() 
	{
		// Check that all other values yield COM_INVALID
		assertEquals(COM_INVALID, NCCommands.stringToCommand("abcdef"));
		assertEquals(COM_INVALID, NCCommands.stringToCommand("12345"));
		assertEquals(COM_INVALID, NCCommands.stringToCommand("_adf_23423$"));
		
		// Check for slightly similar values of commands:
		assertEquals(COM_INVALID, NCCommands.stringToCommand("_" + NCCommands._valid_user_commands_str[0]));
		assertEquals(COM_INVALID, NCCommands.stringToCommand(NCCommands._valid_user_commands_str[0] + "_"));
	}

}
