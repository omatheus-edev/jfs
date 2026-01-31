package codes.matheus.cli;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class CommandTest {
    @Test
    void testCommandCreation() {
        @NotNull Command cmd = Command.create("zip pasta/arquivo.txt");
        assertEquals(Command.Type.ENCODING, cmd.getType());
        assertEquals("zip", cmd.getAction());
        assertTrue(cmd.hasAnyArg());
        assertEquals("pasta/arquivo.txt", cmd.getArg(0));
    }

    @Test
    void testQuotedPath() {
        @NotNull Command cmd = Command.create("unzip \"maze runner/arquivo.zip\"");
        assertEquals("unzip", cmd.getAction());
        assertEquals("maze runner/arquivo.zip", cmd.getArg(0));
    }

    @Test
    void testMultipleArgs() {
        @NotNull Command cmd = Command.create("mv games/action/gow-2018 games/mytology/");
        assertEquals("mv", cmd.getAction());
        assertEquals("games/action/gow-2018", cmd.getArg(0));
        assertEquals("games/mytology/", cmd.getArg(1));
    }

    @Test
    void testNavigationCommand() {
        @NotNull Command cmd = Command.create("ls");

        assertEquals(Command.Type.NAVIGATION, cmd.getType());
        assertEquals("ls", cmd.getAction());
        assertFalse(cmd.hasAnyArg());
    }

    @Test
    void testCryptoCommands() {
        @NotNull Command encryptCmd = Command.create("crypt arquivo.txt");
        @NotNull Command decryptCmd = Command.create("decrypt arquivo.txt.enc");

        assertEquals(Command.Type.ENCODING, encryptCmd.getType());
        assertEquals(Command.Type.ENCODING, decryptCmd.getType());
        assertEquals("arquivo.txt", encryptCmd.getArg(0));
    }

    @Test
    void testInvalidInputs() {
        @NotNull Command unknown = Command.create("commandInf");
        @NotNull Command empty = Command.create("   ");

        assertEquals(Command.Type.UNKNOWN, unknown.getType());
        assertEquals(Command.Type.UNKNOWN, empty.getType());
    }

    @Test
    void testCommandEquality() {
        @NotNull Command cmd1 = Command.create("ls Downloads");
        @NotNull Command cmd2 = Command.create("ls Downloads");

        assertEquals(cmd1, cmd2);
        assertEquals(cmd1.hashCode(), cmd2.hashCode());
    }
}