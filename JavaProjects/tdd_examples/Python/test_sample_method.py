from sample_methods_module import *


def test_add_two_numbers_happy_path():
    input_one = 10
    input_two = 20
    result = add_two_numbers(input_one, input_two)
    expected = 30
    assert result == expected, "Sum of two should be 30"


def test_add_two_numbers_str_input():
    input_one = "909"
    input_two = 52885715
    assert (
        add_two_numbers(input_one, input_two) == -1
    ), "If one input is string return -1"


def test_sum_all_integers():
    """
    Test that it can sum a list of integers
    """
    data = [1, 2, 3]
    result = sum_all(data)
    assert result == 6, "Sum of all should be 6"


def test_sum_all_mixed():
    """
    Test that it can sum a list of mix data
    """
    data = [1, "2", "sp,ee"]
    result = sum_all(data)
    assert result == 1, "Sum of all should be 1"


def test_calculator_add_for_two_numbers():
    """
    Test that it can add two numbers in string
    """
    data = "1,2"
    result = calculator_add(data)
    assert result == 3, "Add for two would be 3"


def test_calculator_add_for_one_number():
    """
    Test that it return the number
    """
    data = "1"
    result = calculator_add(data)
    assert result == 1, "Add for one would be 1"
