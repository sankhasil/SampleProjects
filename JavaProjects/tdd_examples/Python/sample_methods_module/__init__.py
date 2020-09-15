def add_two_numbers(inputOne,inputTwo):
    if type(inputOne) is int and type(inputTwo) is int:
        return inputOne+inputTwo
    else:
        return -1    

def sum_all(arg):
    total = 0
    for val in arg:
        total += val
    return total

# String Calculator : A TDD exercise by Roy Osherove
def calculator_add(numbers):
    splitedByComma = numbers.split(",")
    listLength = len(splitedByComma)
    print(listLength)
    if listLength > 1:
        if listLength == 2:
            return int(splitedByComma[0])+int(splitedByComma[1])
    elif listLength == 1:
        return int(splitedByComma[0])

    return 0