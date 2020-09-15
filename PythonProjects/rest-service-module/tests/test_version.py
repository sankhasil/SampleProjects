"""Test file to check that version for package is available."""
import aiserve


def test_get_version():
    """Check that the version variable is accessible."""
    project_version = aiserve.__version__
    assert project_version is not None and project_version != ""
