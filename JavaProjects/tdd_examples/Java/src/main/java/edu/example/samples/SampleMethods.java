package edu.example.samples;

/**
 * 
 * @author SankyS
 *
 */
public class SampleMethods {

  
  public int addTwoNumbers(int inputOne,int inputTwo) {
    return inputOne+inputTwo;
  }

  public int add(String numbers) {
    if (numbers.isEmpty()) {
      return 0;
    }

    String[] numbersArray = numbers.split(",");
    if (numbersArray.length == 1) {
      return Integer.parseInt(numbers);
    }

    return Integer.parseInt(numbersArray[0]) + Integer.parseInt(numbersArray[1]);
  }
}
