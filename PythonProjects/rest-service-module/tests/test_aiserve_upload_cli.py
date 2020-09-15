"""Test suite for the cli."""
import subprocess


def test_cli_help():
    """Test CLI."""
    result = subprocess.run(
        ["aiserve-upload", "--help", "--debug"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=True,
    )
    assert result.returncode == 0
    assert result.stderr == b""
